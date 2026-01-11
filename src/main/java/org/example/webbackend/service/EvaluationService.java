package org.example.webbackend.service;



import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.example.webbackend.dto.EvaluateRequest;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {

    private static final String PHOA   = "http://example.org/phoa#";
    private static final String SCHEMA = "https://schema.org/";

    private static final Resource PHOA_NOTIFICATION = ResourceFactory.createResource(PHOA + "Notification");

    private static final Property PHOA_NOTIFIED_USER       = ResourceFactory.createProperty(PHOA + "notifiedUser");
    private static final Property PHOA_NOTIFICATION_CONTEXT= ResourceFactory.createProperty(PHOA + "notificationContext");
    private static final Property PHOA_DELIVERED_INTERVENTION = ResourceFactory.createProperty(PHOA + "deliveredIntervention");
    private static final Property PHOA_CONFIDENCE          = ResourceFactory.createProperty(PHOA + "confidence");

    private static final Property PHOA_DETECTED_PHOBIA =
            ResourceFactory.createProperty(PHOA + "detectedPhobia");

    private static final Property SCHEMA_DATE_CREATED      = ResourceFactory.createProperty(SCHEMA + "dateCreated");

    private final RdfStore store;
    private final ContextQueryService contextQueryService;

    public EvaluationService(RdfStore store, ContextQueryService contextQueryService) {
        this.store = store;
        this.contextQueryService = contextQueryService;
    }

    public String evaluateUser(String userId, EvaluateRequest req) {
        var latestCtx = contextQueryService.getLatestContext(userId);
        if (latestCtx == null || latestCtx.contextUri == null) return null;

        Long hr       = getLatestLongObservation(userId, "heartRateBpm");
        Long fear     = getLatestLongObservation(userId, "fearRating");     // optional
        Long noise    = getLatestLongObservation(userId, "noiseLevelDb");   // optional
        Long altitude = getLatestLongObservation(userId, "altitudeMeters"); // optional

        String event = latestCtx.eventName == null ? "" : latestCtx.eventName.toLowerCase();
        String place = latestCtx.placeName == null ? "" : latestCtx.placeName.toLowerCase();

        List<String> phobias = getUserPhobiaTypes(userId);
        if (phobias.isEmpty()) return null;

        String bestPhobiaLocal = null;
        double bestConfidence = 0.0;

        for (String phobiaUri : phobias) {
            String phobiaLocal = phobiaUri.substring(phobiaUri.indexOf('#') + 1);

            if (!matchesContext(phobiaLocal, event, place)) continue;

            double confidence = computeConfidence(phobiaLocal, hr, fear, noise, altitude);
            if (confidence > bestConfidence) {
                bestConfidence = confidence;
                bestPhobiaLocal = phobiaLocal;
            }
        }

        if (bestPhobiaLocal == null) return null;


        List<String> interventions = getRecommendedInterventions(bestPhobiaLocal);

        String notifId = "Notif_" + Instant.now().toString().replaceAll("[:\\.Z-]", "") + "_" +
                UUID.randomUUID().toString().substring(0, 8);
        String notifUri = PHOA + notifId;

        var ds = store.dataset();
        ds.begin(ReadWrite.WRITE);
        try {
            Model m = ds.getDefaultModel();
            Resource notif = m.createResource(notifUri);
            Resource user  = m.createResource(PHOA + userId);
            Resource ctx   = m.createResource(latestCtx.contextUri);

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

            ds.commit();
            return notifUri;
        } finally {
            ds.end();
        }
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

    private List<String> getRecommendedInterventions(String phobiaLocalName) {
        String sparql = """
            PREFIX phoa: <http://example.org/phoa#>
            SELECT ?i WHERE {
              phoa:%s phoa:recommendedIntervention ?i .
            }
        """.formatted(phobiaLocalName);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            List<String> out = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();
                out.add(row.get("i").toString());
            }
            return out;
        } finally {
            ds.end();
        }
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

    private double computeConfidence(String phobiaLocal, Long hr, Long fear, Long noise, Long altitude) {
        double c = 0.55;

        if (fear != null) {
            if (fear >= 8) c += 0.30;
            else if (fear >= 6) c += 0.20;
            else if (fear >= 4) c += 0.10;
        }

        if (hr != null && hr > 100) c += 0.15;

        if ("Agoraphobia".equals(phobiaLocal) && noise != null && noise >= 75) c += 0.10;
        if ("Acrophobia".equals(phobiaLocal) && altitude != null && altitude >= 20) c += 0.10;

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

