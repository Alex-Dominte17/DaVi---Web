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

        SELECT DISTINCT ?phobia ?phobiaLabel ?severity ?intervention ?interventionLabel
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

            Map<String, Map<String, Object>> byPhobia = new LinkedHashMap<>();
            Map<String, Set<String>> seenInterventions = new HashMap<>();

            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();

                String phobiaIri = row.contains("phobia") ? row.get("phobia").toString() : null;
                if (phobiaIri == null) continue;

                Map<String, Object> ph = byPhobia.computeIfAbsent(phobiaIri, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("phobia", phobiaIri);
                    m.put("phobiaLabel", row.contains("phobiaLabel") ? row.get("phobiaLabel").asLiteral().getString() : null);
                    m.put("severity", row.contains("severity") ? row.get("severity").asLiteral().getInt() : null);
                    m.put("interventions", new ArrayList<Map<String, Object>>());
                    return m;
                });

                String intvIri = row.contains("intervention") ? row.get("intervention").toString() : null;
                if (intvIri != null) {
                    seenInterventions.computeIfAbsent(phobiaIri, k -> new HashSet<>());
                    if (seenInterventions.get(phobiaIri).add(intvIri)) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> ints = (List<Map<String, Object>>) ph.get("interventions");

                        Map<String, Object> im = new LinkedHashMap<>();
                        im.put("intervention", intvIri);
                        im.put("interventionLabel", row.contains("interventionLabel")
                                ? row.get("interventionLabel").asLiteral().getString()
                                : null);
                        ints.add(im);
                    }
                }
            }

            return new ArrayList<>(byPhobia.values());
        } finally {
            ds.end();
        }
    }
}
