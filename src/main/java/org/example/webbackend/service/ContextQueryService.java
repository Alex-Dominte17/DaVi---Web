package org.example.webbackend.service;



import org.apache.jena.query.*;
import org.example.webbackend.dto.LatestContextResponse;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContextQueryService {

    private static final String PHOA = "http://example.org/phoa#";

    private final RdfStore store;

    public ContextQueryService(RdfStore store) {
        this.store = store;
    }

    public LatestContextResponse getLatestContext(String userId) {
        String userUri = PHOA + userId;

        String sparql = """
        PREFIX phoa:   <http://example.org/phoa#>
        PREFIX schema: <https://schema.org/>
        PREFIX time:   <http://www.w3.org/2006/time#>
        PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

        SELECT ?ctx ?t ?eventName ?placeName ?lat ?lon ?seasonLabel ?sourceLabel ?addressed
        WHERE {
          <%s> phoa:hasContext ?ctx .

          OPTIONAL {
            ?ctx phoa:contextTime ?timeNode .
            ?timeNode time:inXSDDateTime ?t .
          }

          OPTIONAL { ?ctx phoa:addressed ?addressed . }

          OPTIONAL {
            ?ctx phoa:contextEvent ?ev .
            OPTIONAL { ?ev schema:name ?eventName . }
          }

          OPTIONAL {
            ?ctx phoa:contextLocation ?place .
            OPTIONAL { ?place schema:name ?placeName . }
            OPTIONAL {
              ?place schema:geo ?geo .
              OPTIONAL { ?geo schema:latitude ?lat . }
              OPTIONAL { ?geo schema:longitude ?lon . }
            }
          }

          OPTIONAL {
            ?ctx phoa:contextSeason ?season .
            OPTIONAL { ?season rdfs:label ?seasonLabel . }
          }

          OPTIONAL {
            ?ctx phoa:reportedBy ?src .
            OPTIONAL { ?src rdfs:label ?sourceLabel . }
          }
        }
        ORDER BY DESC(?t)
        LIMIT 1
    """.formatted(userUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            if (!rs.hasNext()) return null;

            QuerySolution row = rs.nextSolution();
            LatestContextResponse out = new LatestContextResponse();

            out.contextUri = row.contains("ctx") ? row.get("ctx").toString() : null;
            out.time = row.contains("t") ? row.get("t").asLiteral().getString() : null;

            out.eventName = row.contains("eventName") ? row.get("eventName").asLiteral().getString() : null;
            out.placeName = row.contains("placeName") ? row.get("placeName").asLiteral().getString() : null;

            out.seasonLabel = row.contains("seasonLabel") ? row.get("seasonLabel").asLiteral().getString() : null;
            out.sourceLabel = row.contains("sourceLabel") ? row.get("sourceLabel").asLiteral().getString() : null;

            out.lat = row.contains("lat") ? row.get("lat").asLiteral().getDouble() : null;
            out.lon = row.contains("lon") ? row.get("lon").asLiteral().getDouble() : null;

            out.addressed = row.contains("addressed") && row.get("addressed").isLiteral()
                    ? row.get("addressed").asLiteral().getBoolean()
                    : false;

            return out;
        } finally {
            ds.end();
        }
    }


    public List<LatestContextResponse> listUnaddressedContexts(String userId, int limit) {
        String userUri = PHOA + userId;

        String sparql = """
        PREFIX phoa:   <http://example.org/phoa#>
        PREFIX schema: <https://schema.org/>
        PREFIX time:   <http://www.w3.org/2006/time#>
        PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

        SELECT ?ctx ?t ?eventName ?placeName ?lat ?lon ?seasonLabel ?sourceLabel
        WHERE {
          <%s> phoa:hasContext ?ctx .

          FILTER NOT EXISTS { ?ctx phoa:addressed true }

          OPTIONAL {
            ?ctx phoa:contextTime ?timeNode .
            ?timeNode time:inXSDDateTime ?t .
          }

          OPTIONAL {
            ?ctx phoa:contextEvent ?ev .
            OPTIONAL { ?ev schema:name ?eventName . }
          }

          OPTIONAL {
            ?ctx phoa:contextLocation ?place .
            OPTIONAL { ?place schema:name ?placeName . }
            OPTIONAL {
              ?place schema:geo ?geo .
              OPTIONAL { ?geo schema:latitude ?lat . }
              OPTIONAL { ?geo schema:longitude ?lon . }
            }
          }

          OPTIONAL {
            ?ctx phoa:contextSeason ?season .
            OPTIONAL { ?season rdfs:label ?seasonLabel . }
          }

          OPTIONAL {
            ?ctx phoa:reportedBy ?src .
            OPTIONAL { ?src rdfs:label ?sourceLabel . }
          }
        }
        ORDER BY ASC(?t)
        LIMIT %d
    """.formatted(userUri, Math.max(1, limit));

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            List<LatestContextResponse> out = new ArrayList<>();

            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();
                LatestContextResponse ctx = new LatestContextResponse();

                ctx.contextUri = row.contains("ctx") ? row.get("ctx").toString() : null;
                ctx.time = row.contains("t") ? row.get("t").asLiteral().getString() : null;

                ctx.eventName = row.contains("eventName") ? row.get("eventName").asLiteral().getString() : null;
                ctx.placeName = row.contains("placeName") ? row.get("placeName").asLiteral().getString() : null;

                ctx.seasonLabel = row.contains("seasonLabel") ? row.get("seasonLabel").asLiteral().getString() : null;
                ctx.sourceLabel = row.contains("sourceLabel") ? row.get("sourceLabel").asLiteral().getString() : null;

                ctx.lat = row.contains("lat") ? row.get("lat").asLiteral().getDouble() : null;
                ctx.lon = row.contains("lon") ? row.get("lon").asLiteral().getDouble() : null;

                out.add(ctx);
            }

            return out;
        } finally {
            ds.end();
        }
    }

}
