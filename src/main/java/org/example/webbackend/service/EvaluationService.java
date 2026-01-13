package org.example.webbackend.service;



import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.example.webbackend.dto.EvaluateRequest;
import org.example.webbackend.dto.LatestContextResponse;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {
    private final EmailService emailService;


    private static final String PHOA   = "http://example.org/phoa#";
    private static final String SCHEMA = "https://schema.org/";

    private static final Resource PHOA_NOTIFICATION = ResourceFactory.createResource(PHOA + "Notification");
    private static final Property PHOA_CONTEXT_ADDRESSED =
            ResourceFactory.createProperty(PHOA + "addressed");

    private static final Property PHOA_NOTIFIED_USER       = ResourceFactory.createProperty(PHOA + "notifiedUser");
    private static final Property PHOA_NOTIFICATION_CONTEXT= ResourceFactory.createProperty(PHOA + "notificationContext");
    private static final Property PHOA_DELIVERED_INTERVENTION = ResourceFactory.createProperty(PHOA + "deliveredIntervention");
    private static final Property PHOA_CONFIDENCE          = ResourceFactory.createProperty(PHOA + "confidence");

    private static final Property PHOA_DETECTED_PHOBIA =
            ResourceFactory.createProperty(PHOA + "detectedPhobia");

    private static final Property SCHEMA_DATE_CREATED      = ResourceFactory.createProperty(SCHEMA + "dateCreated");

    private final RdfStore store;
    private final ContextQueryService contextQueryService;

    public EvaluationService(RdfStore store, ContextQueryService contextQueryService,EmailService emailService) {
        this.store = store;
        this.contextQueryService = contextQueryService;
        this.emailService = emailService;
    }

    private List<String> getAlertEmailsByUserPrefix(Model m, String userLocalName) {
        String prefix = PHOA + "Contact_" + userLocalName + "_";

        String sparql = """
        PREFIX phoa: <http://example.org/phoa#>
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        SELECT ?mbox WHERE {
          ?c phoa:alertsEnabled true ;
             foaf:mbox ?mbox .
          FILTER( STRSTARTS(STR(?c), "%s") )
        }
    """.formatted(prefix);

        List<String> emails = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, m)) {
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                String mbox = rs.nextSolution().get("mbox").toString(); // "mailto:..."
                String email = mbox.startsWith("mailto:") ? mbox.substring("mailto:".length()) : mbox;
                emails.add(email);
            }
        }
        return emails;
    }




    public List<String> evaluateUserAllContexts(String userId, EvaluateRequest req, int limit) {
        List<String> created = new ArrayList<>();

        List<LatestContextResponse> contexts = contextQueryService.listUnaddressedContexts(userId, limit);
        if (contexts == null || contexts.isEmpty()) return created;

        Long hr       = getLatestLongObservation(userId, "heartRateBpm");
        Long fear     = getLatestLongObservation(userId, "fearRating");
        Long noise    = getLatestLongObservation(userId, "noiseLevelDb");
        Long altitude = getLatestLongObservation(userId, "altitudeMeters");
        Long pleasureAnxiety = getLatestLongObservation(userId, "pleasureAnxietyRating");

        List<String> phobias = getUserPhobiaTypes(userId);
        if (phobias == null || phobias.isEmpty()) return created;

        for (var ctxDto : contexts) {
            if (ctxDto == null || ctxDto.contextUri == null) continue;

            String event = ctxDto.eventName == null ? "" : ctxDto.eventName.toLowerCase();
            String place = ctxDto.placeName == null ? "" : ctxDto.placeName.toLowerCase();

            String bestPhobiaLocal = null;
            double bestConfidence = 0.0;

            for (String phobiaUri : phobias) {
                String phobiaLocal = phobiaUri.substring(phobiaUri.indexOf('#') + 1);
                if (!matchesContext(phobiaLocal, event, place)) continue;

                double confidence = computeConfidence(phobiaLocal, hr, fear, noise, altitude, pleasureAnxiety);
                if (confidence > bestConfidence) {
                    bestConfidence = confidence;
                    bestPhobiaLocal = phobiaLocal;
                }
            }

            // We'll prepare email data here, send AFTER commit/end
            List<String> emailRecipients = Collections.emptyList();
            String emailSubject = null;
            String emailBody = null;

            Dataset ds = store.dataset();
            ds.begin(ReadWrite.WRITE);
            try {
                Model m = ds.getDefaultModel();
                Resource ctx = m.createResource(ctxDto.contextUri);

                // Guard 1: already addressed?
                Statement addressedStmt = ctx.getProperty(PHOA_CONTEXT_ADDRESSED);
                if (addressedStmt != null
                        && addressedStmt.getObject().isLiteral()
                        && addressedStmt.getObject().asLiteral().getBoolean()) {
                    ds.commit();
                    continue;
                }

                // Guard 2: notif already exists for this context?
                boolean notifExists = m.listResourcesWithProperty(PHOA_NOTIFICATION_CONTEXT, ctx).hasNext();
                if (notifExists) {
                    ctx.removeAll(PHOA_CONTEXT_ADDRESSED);
                    ctx.addLiteral(PHOA_CONTEXT_ADDRESSED, true);
                    ds.commit();
                    continue;
                }

                // Mark addressed
                ctx.removeAll(PHOA_CONTEXT_ADDRESSED);
                ctx.addLiteral(PHOA_CONTEXT_ADDRESSED, true);

                if (bestPhobiaLocal != null) {
                    List<String> interventions = getRecommendedInterventions(m, bestPhobiaLocal);

                    String notifId = "Notif_" + Instant.now().toString().replaceAll("[:\\.Z-]", "") + "_"
                            + UUID.randomUUID().toString().substring(0, 8);
                    String notifUri = PHOA + notifId;

                    Resource notif = m.createResource(notifUri);
                    Resource user  = m.createResource(PHOA + userId);

                    notif.addProperty(RDF.type, PHOA_NOTIFICATION);
                    notif.addProperty(PHOA_NOTIFIED_USER, user);
                    notif.addProperty(PHOA_NOTIFICATION_CONTEXT, ctx);
                    notif.addLiteral(PHOA_CONFIDENCE, m.createTypedLiteral(bestConfidence));
                    notif.addProperty(SCHEMA_DATE_CREATED,
                            m.createTypedLiteral(Instant.now().toString(), XSDDatatype.XSDdateTime));
                    notif.addProperty(PHOA_DETECTED_PHOBIA, m.createResource(PHOA + bestPhobiaLocal));

                    for (String iUri : interventions) {
                        notif.addProperty(PHOA_DELIVERED_INTERVENTION, m.createResource(iUri));
                    }

                    created.add(notifUri);

                    // Prepare email recipients + message (fast RDF read, still inside tx)
                    emailRecipients = getAlertEmailsByUserPrefix(m, userId); // userId must be "Alice" style local name

                    if (!emailRecipients.isEmpty()) {
                        emailSubject = "PHOA alert for " + userId + ": " + bestPhobiaLocal;

                        String ev = (ctxDto.eventName == null ? "-" : ctxDto.eventName);
                        String pl = (ctxDto.placeName == null ? "-" : ctxDto.placeName);

                        emailBody =
                                "A new PHOA notification was created.\n\n" +
                                        "User: " + userId + "\n" +
                                        "Context: " + ev + " / " + pl + "\n" +
                                        "Detected phobia: " + bestPhobiaLocal + "\n" +
                                        "Confidence: " + bestConfidence + "\n" +
                                        "Recommended interventions: " + (interventions == null ? "-" : String.join(", ", interventions)) + "\n" +
                                        "Notification URI: " + notifUri + "\n";
                    }
                }

                ds.commit();
            } catch (Exception e) {
                ds.abort();
                throw e;
            } finally {
                ds.end();
            }

            // Send emails AFTER transaction commit/end
            if (emailSubject != null && emailBody != null && emailRecipients != null && !emailRecipients.isEmpty()) {
                for (String to : emailRecipients) {
                    try {
                        emailService.sendAsync(to, emailSubject, emailBody);
                    } catch (Exception mailEx) {
                        // Don't fail the whole request because one email fails
                        System.err.println("Failed to send email to " + to + ": " + mailEx.getMessage());
                    }
                }
            }
        }

        return created;
    }

    private boolean userHasPhobia(String userId, String phobiaLocalName) {
        String sparql = """
            PREFIX phoa: <http://example.org/phoa#>
            ASK {
              phoa:%s phoa:hasPhobiaAffliction ?aff .
              ?aff phoa:phobiaType phoa:%s .
            }
        """.formatted(userId, phobiaLocalName);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            return qexec.execAsk();
        } finally {
            ds.end();
        }
    }

    private Long getLatestLongObservation(String userId, String property) {
        String userUri = PHOA + userId;
        String propUri = PHOA + property;

        String sparql = """
            PREFIX phoa: <http://example.org/phoa#>
            PREFIX sosa: <http://www.w3.org/ns/sosa/>
            SELECT ?value ?time WHERE {
              <%s> phoa:hasObservation ?obs .
              ?obs sosa:observedProperty <%s> ;
                   sosa:hasSimpleResult ?value ;
                   sosa:resultTime ?time .
            }
            ORDER BY DESC(?time)
            LIMIT 1
        """.formatted(userUri, propUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            if (!rs.hasNext()) return null;
            QuerySolution row = rs.nextSolution();
            if (!row.get("value").isLiteral()) return null;
            try {
                return row.get("value").asLiteral().getLong();
            } catch (Exception e) {
                return null;
            }
        } finally {
            ds.end();
        }
    }

    private List<String> getRecommendedInterventions(Model m, String phobiaLocalName) {
        String sparql = """
        PREFIX phoa: <http://example.org/phoa#>
        SELECT ?i WHERE {
          phoa:%s phoa:recommendedIntervention ?i .
        }
    """.formatted(phobiaLocalName);

        List<String> out = new ArrayList<>();

        // Use the model (no begin/end here)
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, m)) {
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();
                out.add(row.get("i").toString());
            }
        }

        return out;
    }

    private List<String> getUserPhobiaTypes(String userId) {
        String sparql = """
      PREFIX phoa: <http://example.org/phoa#>
      SELECT DISTINCT ?phobia WHERE {
        phoa:%s phoa:hasPhobiaAffliction ?aff .
        ?aff phoa:phobiaType ?phobia .
      }
    """.formatted(userId);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            List<String> out = new ArrayList<>();
            while (rs.hasNext()) out.add(rs.nextSolution().get("phobia").toString());
            return out;
        } finally { ds.end(); }
    }

    private double computeConfidence(String phobiaLocal,
                                     Long hr,
                                     Long fear,
                                     Long noise,
                                     Long altitude,
                                     Long pleasureAnxiety) {
        double c = 0.55;

        if (fear != null) {
            if (fear >= 8) c += 0.30;
            else if (fear >= 6) c += 0.20;
            else if (fear >= 4) c += 0.10;
        }

        if (hr != null && hr > 100) c += 0.15;

        if ("Agoraphobia".equals(phobiaLocal) && noise != null && noise >= 75) c += 0.10;
        if ("Acrophobia".equals(phobiaLocal) && altitude != null && altitude >= 20) c += 0.10;

        // NEW: Hedonophobia signal from self-report
        if ("Hedonophobia".equals(phobiaLocal) && pleasureAnxiety != null) {
            if (pleasureAnxiety >= 8) c += 0.25;
            else if (pleasureAnxiety >= 6) c += 0.15;
            else if (pleasureAnxiety >= 4) c += 0.07;
        }
        System.out.println(phobiaLocal+"---"+c);
        return Math.min(0.95, c);
    }


    private boolean matchesContext(String phobiaLocal, String event, String place) {
        String text = (event + " " + place);

        switch (phobiaLocal) {
            case "Claustrophobia":
                return text.contains("elevator")
                        || text.contains("enclosed")
                        || text.contains("small")
                        || text.contains("tight")
                        || text.contains("locked");

            case "Acrophobia":
                return text.contains("balcony")
                        || text.contains("roof")
                        || text.contains("bridge")
                        || text.contains("height")
                        || text.contains("high altitude")
                        || text.contains("stairs");

            case "Arachnophobia":
                return text.contains("spider")
                        || text.contains("basement")
                        || text.contains("attic")
                        || text.contains("storage")
                        || text.contains("shed");
            case "Hedonophobia":
                return text.contains("enjoy")
                        || text.contains("relax")
                        || text.contains("fun")
                        || text.contains("pleasure")
                        || text.contains("treat")
                        || text.contains("dessert")
                        || text.contains("party")
                        || text.contains("celebrat")
                        || text.contains("vacation")
                        || text.contains("spa")
                        || text.contains("date")
                        || text.contains("cafe")
                        || text.contains("restaurant");


            case "Agoraphobia":
                return text.contains("mall")
                        || text.contains("crowd")
                        || text.contains("supermarket")
                        || text.contains("station")
                        || text.contains("metro")
                        || text.contains("bus");

            default:
                return false;
        }
    }

}

