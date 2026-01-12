package org.example.webbackend.service;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;
import org.example.webbackend.dto.AddUserPhobiaRequest;
import org.example.webbackend.dto.AddUserPhobiaResponse;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Objects;

@Service
public class UserPhobiaService {

    private static final String PHOA_NS = "http://example.org/phoa#";

    private final RdfStore store;

    public UserPhobiaService(RdfStore store) {
        this.store = store;
    }

    public AddUserPhobiaResponse addPhobiaToUser(String userId, AddUserPhobiaRequest req) {
        if (req == null || req.wikidataUri == null || req.wikidataUri.isBlank()) {
            throw new IllegalArgumentException("wikidataUri is required");
        }
        if (req.label == null || req.label.isBlank()) {
            throw new IllegalArgumentException("label is required (send it from /phobias/suggest or /phobias/random)");
        }

        String userUri = PHOA_NS + userId;
        String wikidataUri = req.wikidataUri.trim();
        String label = req.label.trim();

        Dataset ds = store.dataset();

        String existingPhobiaUri = Txn.calculateRead(ds, () -> findLocalPhobiaByWikidata(ds, wikidataUri));

        String phobiaUri = (existingPhobiaUri != null) ? existingPhobiaUri : (PHOA_NS + slugify(label));

        if (existingPhobiaUri == null) {
            final String baseIri = phobiaUri;
            phobiaUri = Txn.calculateRead(ds, () -> ensureUniqueIri(ds, baseIri));
        }

        final String finalPhobiaUri = phobiaUri;

        Txn.executeWrite(ds, () -> {
            Model m = ds.getDefaultModel();

            Resource user = m.createResource(userUri);

            Resource userClass = m.createResource(PHOA_NS + "User");
            Resource phobiaClass = m.createResource(PHOA_NS + "Phobia");
            Resource afflictionClass = m.createResource(PHOA_NS + "PhobiaAffliction");

            Property hasAffliction = m.createProperty(PHOA_NS, "hasPhobiaAffliction");
            Property phobiaTypeP = m.createProperty(PHOA_NS, "phobiaType");
            Property afflictionStartP = m.createProperty(PHOA_NS, "afflictionStart");
            Property severityP = m.createProperty(PHOA_NS, "severity");

            m.add(user, RDF.type, userClass);

            Resource phobia = m.createResource(finalPhobiaUri);

            if (existingPhobiaUri == null) {
                Property labelP = m.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
                Property exactMatch = m.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch");

                m.add(phobia, RDF.type, phobiaClass);
                m.add(phobia, labelP, m.createLiteral(label, "en"));
                m.add(phobia, exactMatch, m.createResource(wikidataUri));
            } else {
                if (!m.contains(phobia, RDF.type, phobiaClass)) {
                    m.add(phobia, RDF.type, phobiaClass);
                }
            }

            Resource existingAffliction = null;
            StmtIterator it = m.listStatements(user, hasAffliction, (RDFNode) null);
            while (it.hasNext()) {
                Statement st = it.nextStatement();
                RDFNode obj = st.getObject();
                if (!obj.isResource()) continue;

                Resource aff = obj.asResource();
                if (m.contains(aff, RDF.type, afflictionClass) && m.contains(aff, phobiaTypeP, phobia)) {
                    existingAffliction = aff;
                    break;
                }
            }
            it.close();

            if (existingAffliction == null) {
                String afflictionUri = PHOA_NS + slugify(userId) + slugify(label) + "Affliction";
                Resource aff = m.createResource(afflictionUri);

                m.add(aff, RDF.type, afflictionClass);
                m.add(aff, phobiaTypeP, phobia);

                String start = (req.afflictionStart != null && !req.afflictionStart.isBlank())
                        ? req.afflictionStart.trim()
                        : java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString();

                m.add(aff, afflictionStartP,
                        m.createTypedLiteral(start, "http://www.w3.org/2001/XMLSchema#dateTime"));

                int sev = (req.severity != null) ? req.severity : 5;
                m.add(aff, severityP, m.createTypedLiteral(sev));

                m.add(user, hasAffliction, aff);
            }
        });

        return new AddUserPhobiaResponse(userUri, phobiaUri, wikidataUri, label);
    }

    private static String findLocalPhobiaByWikidata(Dataset ds, String wikidataUri) {
        String sparql = """
            PREFIX skos:   <http://www.w3.org/2004/02/skos/core#>
            PREFIX phoa:   <http://example.org/phoa#>

            SELECT ?p
            WHERE {
              ?p a phoa:Phobia ;
                 skos:exactMatch <%s> .
            }
            LIMIT 1
            """.formatted(wikidataUri);

        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            if (rs.hasNext()) {
                return rs.nextSolution().getResource("p").getURI();
            }
            return null;
        }
    }

    private static String ensureUniqueIri(Dataset ds, String baseIri) {
        Model m = ds.getDefaultModel();
        if (!m.containsResource(m.createResource(baseIri))) return baseIri;

        for (int i = 2; i <= 9999; i++) {
            String candidate = baseIri + "_" + i;
            if (!m.containsResource(m.createResource(candidate))) return candidate;
        }
        return baseIri + "_" + System.currentTimeMillis();
    }

    private static String slugify(String input) {
        String s = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        s = s.replaceAll("[^A-Za-z0-9]+", "_");
        s = s.replaceAll("^_+|_+$", "");
        if (s.isBlank()) s = "Phobia";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

