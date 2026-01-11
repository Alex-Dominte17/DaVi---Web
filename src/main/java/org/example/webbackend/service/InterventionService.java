package org.example.webbackend.service;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.system.Txn;
import org.example.webbackend.dto.Intervention;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterventionService {

    private final RdfStore rdfStore;

    public InterventionService(RdfStore rdfStore) {
        this.rdfStore = rdfStore;
    }

    public List<Intervention> listAllInterventions() {
        String sparql = """
            PREFIX phoa:   <http://example.org/phoa#>
            PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX schema: <https://schema.org/>

            SELECT ?intervention ?type ?label ?url
            WHERE {
              ?intervention rdf:type ?type .
              ?type rdfs:subClassOf* phoa:Intervention .

              OPTIONAL {
                ?intervention rdfs:label ?label .
                FILTER(lang(?label) = "en" || lang(?label) = "")
              }

              OPTIONAL { ?intervention schema:url ?url . }
              OPTIONAL { ?intervention phoa:youtubeLink ?url . }
            }
            ORDER BY ?type ?label
            """;

        // Get the actual Jena Dataset from your store
        var ds = rdfStore.dataset();

        return Txn.calculateRead(ds, () -> {
            List<Intervention> out = new ArrayList<>();

            try (QueryExecution qe = QueryExecutionFactory.create(sparql, ds)) {
                ResultSet rs = qe.execSelect();

                while (rs.hasNext()) {
                    QuerySolution row = rs.next();

                    String iri = row.getResource("intervention").getURI();
                    String typeIri = row.getResource("type").getURI();
                    String label = row.contains("label") ? row.getLiteral("label").getString() : null;

                    String url = null;
                    if (row.contains("url")) {
                        RDFNode n = row.get("url");
                        url = n.isLiteral() ? n.asLiteral().getString() : n.toString();
                    }

                    out.add(new Intervention(iri, typeIri, label, url));
                }
            }

            return out;
        });
    }
}
