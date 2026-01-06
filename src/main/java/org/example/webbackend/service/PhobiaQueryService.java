package org.example.webbackend.service;



import org.apache.jena.query.*;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PhobiaQueryService {

    private static final String PHOA = "http://example.org/phoa#";
    private static final String SCHEMA = "https://schema.org/";

    private final RdfStore store;

    public PhobiaQueryService(RdfStore store) {
        this.store = store;
    }

    public List<Map<String, Object>> getUserPhobias(String userLocalName) {
        String userUri = PHOA + userLocalName;

        String sparql = """
            PREFIX phoa: <http://example.org/phoa#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT ?phobia ?phobiaLabel ?severity ?intervention ?interventionLabel
            WHERE {
              <%s> phoa:hasPhobiaAffliction ?aff .
              ?aff phoa:phobiaType ?phobia .
              OPTIONAL { ?aff phoa:severity ?severity . }
              OPTIONAL { ?phobia rdfs:label ?phobiaLabel . }

              OPTIONAL {
                ?phobia phoa:recommendedIntervention ?intervention .
                OPTIONAL { ?intervention rdfs:label ?interventionLabel . }
              }
            }
        """.formatted(userUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("phobia", row.contains("phobia") ? row.get("phobia").toString() : null);
                m.put("phobiaLabel", row.contains("phobiaLabel") ? row.get("phobiaLabel").asLiteral().getString() : null);
                m.put("severity", row.contains("severity") ? row.get("severity").asLiteral().getInt() : null);
                m.put("intervention", row.contains("intervention") ? row.get("intervention").toString() : null);
                m.put("interventionLabel", row.contains("interventionLabel") ? row.get("interventionLabel").asLiteral().getString() : null);
                out.add(m);
            }
            return out;
        } finally {
            ds.end();
        }
    }
}
