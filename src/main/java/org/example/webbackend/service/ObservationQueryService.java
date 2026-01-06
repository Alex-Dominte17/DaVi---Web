package org.example.webbackend.service;


import org.apache.jena.query.*;
import org.example.webbackend.dto.LatestObservationResponse;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

@Service
public class ObservationQueryService {

    private static final String PHOA   = "http://example.org/phoa#";
    private static final String SCHEMA = "https://schema.org/";

    private final RdfStore store;

    public ObservationQueryService(RdfStore store) {
        this.store = store;
    }

    public LatestObservationResponse getLatestObservation(String userId, String property) {
        String userUri = PHOA + userId;

        String propertyUri = property.startsWith("http")
                ? property
                : SCHEMA + property;

        String sparql = """
            PREFIX phoa: <http://example.org/phoa#>
            PREFIX sosa: <http://www.w3.org/ns/sosa/>
            PREFIX schema: <https://schema.org/>

            SELECT ?obs ?value ?unit ?time ?sensor
            WHERE {
              <%s> phoa:hasObservation ?obs .
              ?obs sosa:observedProperty <%s> ;
                   sosa:hasSimpleResult ?value ;
                   sosa:resultTime ?time .

              OPTIONAL {
                ?obs sosa:madeBySensor ?sensor .
              }
              OPTIONAL {
                ?obs schema:unitText ?unit .
              }
            }
            ORDER BY DESC(?time)
            LIMIT 1
        """.formatted(userUri, propertyUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            if (!rs.hasNext()) return null;

            QuerySolution row = rs.nextSolution();
            LatestObservationResponse out = new LatestObservationResponse();

            out.observationUri = row.get("obs").toString();
            out.observedProperty = propertyUri;
            out.value = row.get("value").asLiteral().getLexicalForm();
            out.time = row.get("time").asLiteral().getString();

            if (row.contains("unit")) {
                out.unit = row.get("unit").asLiteral().getString();
            }
            if (row.contains("sensor")) {
                out.sensor = row.get("sensor").toString();
            }

            return out;
        } finally {
            ds.end();
        }
    }
}
