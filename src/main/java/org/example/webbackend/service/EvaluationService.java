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

    private static final Property SCHEMA_DATE_CREATED      = ResourceFactory.createProperty(SCHEMA + "dateCreated");

    private final RdfStore store;
    private final ContextQueryService contextQueryService;

    public EvaluationService(RdfStore store, ContextQueryService contextQueryService) {
        this.store = store;
        this.contextQueryService = contextQueryService;
    }

    public String evaluateUser(String userId, EvaluateRequest req) {
        // 1) get latest context
        var latestCtx = contextQueryService.getLatestContext(userId);
        if (latestCtx == null || latestCtx.contextUri == null) return null;

        // 2) get latest heart rate (optional, but used for confidence)
        Long hr = getLatestLongObservation(userId, "heartRate");

        // 3) check whether user has claustrophobia
        boolean hasClaustrophobia = userHasPhobia(userId, "Claustrophobia");
        if (!hasClaustrophobia) return null;

        // 4) simple trigger heuristic: event/place contains "elevator"
        String event = latestCtx.eventName == null ? "" : latestCtx.eventName.toLowerCase();
        String place = latestCtx.placeName == null ? "" : latestCtx.placeName.toLowerCase();
        boolean inElevator = event.contains("elevator") || place.contains("elevator");
        if (!inElevator) return null;

        // 5) determine confidence
        double confidence = 0.6;
        if (hr != null && hr > 100) confidence = 0.85;

        // 6) fetch recommended interventions from RDF for Claustrophobia
        List<String> interventions = getRecommendedInterventions("Claustrophobia");

        // 7) create Notification RDF node
        String notifId = "Notif_" + Instant.now().toString().replaceAll("[:\\.Z-]", "") + "_" + UUID.randomUUID().toString().substring(0, 8);
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
            notif.addLiteral(PHOA_CONFIDENCE, m.createTypedLiteral(confidence));
            notif.addProperty(SCHEMA_DATE_CREATED, m.createTypedLiteral(Instant.now().toString(), XSDDatatype.XSDdateTime));

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
        String propUri = SCHEMA + property;

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
}

