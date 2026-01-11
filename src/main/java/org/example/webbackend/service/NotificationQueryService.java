package org.example.webbackend.service;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.example.webbackend.dto.NotificationSummary;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationQueryService {

    private static final String PHOA = "http://example.org/phoa#";

    private final RdfStore store;

    public NotificationQueryService(RdfStore store) {
        this.store = store;
    }

    public List<NotificationSummary> listUserNotifications(String userId, int limit) {
        String userUri = PHOA + userId;

        String sparql = """
        PREFIX phoa:   <http://example.org/phoa#>
        PREFIX schema: <https://schema.org/>
        PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX time:   <http://www.w3.org/2006/time#>
    
        SELECT ?n ?created ?conf ?status ?ctx ?eventName ?placeName ?ctxTime
               ?phobia ?phobiaLabel
               ?i ?iLabel ?iType ?iUrl
        WHERE {
          ?n a phoa:Notification ;
             phoa:notifiedUser <%s> .
    
          OPTIONAL { ?n schema:dateCreated ?created . }
          OPTIONAL { ?n phoa:confidence ?conf . }
          OPTIONAL { ?n phoa:status ?status . }
    
          OPTIONAL {
            ?n phoa:detectedPhobia ?phobia .
            OPTIONAL { ?phobia rdfs:label ?phobiaLabel . }
          }
    
          OPTIONAL {
            ?n phoa:notificationContext ?ctx .
    
            OPTIONAL {
              ?ctx phoa:contextEvent ?ev .
              OPTIONAL { ?ev schema:name ?eventName . }
            }
    
            OPTIONAL {
              ?ctx phoa:contextLocation ?place .
              OPTIONAL { ?place schema:name ?placeName . }
            }
    
            OPTIONAL {
              ?ctx phoa:contextTime ?tNode .
              OPTIONAL { ?tNode time:inXSDDateTime ?ctxTime . }
            }
          }
    
          OPTIONAL {
            ?n phoa:deliveredIntervention ?i .
            OPTIONAL { ?i rdfs:label ?iLabel . }

            # Get the specific intervention type (subclass of phoa:Intervention)
            OPTIONAL {
              ?i rdf:type ?iType .
              ?iType rdfs:subClassOf* phoa:Intervention .
            }

            # Get a URL: either schema:url or your phoa:youtubeLink
            OPTIONAL { ?i schema:url ?iUrl . }
            OPTIONAL { ?i phoa:youtubeLink ?iUrl . }
          }
        }
        ORDER BY DESC(?created)
        LIMIT %d
    """.formatted(userUri, Math.max(1, limit));

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();

            Map<String, NotificationSummary> grouped = new LinkedHashMap<>();
            Map<String, Set<String>> seenInterventions = new HashMap<>();

            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();

                String notifUri = row.get("n").toString();
                NotificationSummary n = grouped.computeIfAbsent(notifUri, k -> {
                    NotificationSummary x = new NotificationSummary();
                    x.notificationUri = notifUri;

                    if (row.contains("created")) x.createdAt = row.get("created").asLiteral().getString();
                    if (row.contains("conf")) x.confidence = row.get("conf").asLiteral().getDouble();
                    if (row.contains("phobia")) x.detectedPhobia = row.get("phobia").toString();
                    if (row.contains("phobiaLabel")) x.detectedPhobiaLabel = row.get("phobiaLabel").asLiteral().getString();

                    if (row.contains("ctx")) x.contextUri = row.get("ctx").toString();
                    if (row.contains("eventName")) x.contextEventName = row.get("eventName").asLiteral().getString();
                    if (row.contains("placeName")) x.contextPlaceName = row.get("placeName").asLiteral().getString();
                    if (row.contains("ctxTime")) x.contextTime = row.get("ctxTime").asLiteral().getString();

                    if (row.contains("status")) {
                        x.status = row.get("status").asLiteral().getString();
                    } else {
                        x.status = "unread";
                    }

                    return x;
                });

                if (row.contains("i")) {
                    String iUri = row.get("i").toString();

                    String iLabel = row.contains("iLabel")
                            ? row.get("iLabel").asLiteral().getString()
                            : null;

                    String iTypeIri = row.contains("iType") && row.get("iType").isResource()
                            ? row.getResource("iType").getURI()
                            : (row.contains("iType") ? row.get("iType").toString() : null);

                    String iUrl = null;
                    if (row.contains("iUrl")) {
                        RDFNode u = row.get("iUrl");
                        iUrl = u.isLiteral() ? u.asLiteral().getString() : u.toString();
                    }

                    seenInterventions.computeIfAbsent(notifUri, kk -> new HashSet<>());
                    if (seenInterventions.get(notifUri).add(iUri)) {
                        NotificationSummary.InterventionItem item =
                                new NotificationSummary.InterventionItem(iUri, iTypeIri, iLabel, iUrl);
                        n.interventions.add(item);
                    }
                }
            }

            return new ArrayList<>(grouped.values());
        } finally {
            ds.end();
        }
    }
}

