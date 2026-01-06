package org.example.webbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.query.*;
import org.example.webbackend.dto.PhobiaEnrichmentResponse;
import org.example.webbackend.rdf.RdfStore;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalKbEnrichmentService {

    private static final String PHOA_NS = "http://example.org/phoa#";

    // SPARQL endpoints
    private static final String WIKIDATA_SPARQL = "https://query.wikidata.org/sparql";
    private static final String DBPEDIA_SPARQL  = "https://dbpedia.org/sparql";

    private final RdfStore store;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public ExternalKbEnrichmentService(RdfStore store) {
        this.store = store;
    }

    public PhobiaEnrichmentResponse enrichPhobia(String phobiaId) {
        String phobiaUri = PHOA_NS + phobiaId;

        PhobiaEnrichmentResponse out = new PhobiaEnrichmentResponse();
        out.phobiaUri = phobiaUri;

        // 1) read external links from your RDF graph
        List<String> links = findExternalLinks(phobiaUri);
        out.externalUris.addAll(links);

        // 2) prefer Wikidata if present, else DBpedia
        String wikidataEntity = links.stream()
                .filter(u -> u.contains("wikidata.org/entity/"))
                .findFirst().orElse(null);

        String dbpediaResource = links.stream()
                .filter(u -> u.startsWith("http://dbpedia.org/resource/") || u.startsWith("https://dbpedia.org/resource/"))
                .findFirst().orElse(null);

        try {
            if (wikidataEntity != null) {
                fillFromWikidata(out, wikidataEntity);
                return out;
            }
            if (dbpediaResource != null) {
                fillFromDbpedia(out, dbpediaResource);
                return out;
            }
        } catch (Exception e) {
            // best effort: return whatever we have (externalUris) even if remote fetch fails
            out.source = "none";
            return out;
        }

        out.source = "none";
        return out;
    }

    private List<String> findExternalLinks(String phobiaUri) {
        String sparql = """
            PREFIX skos:   <http://www.w3.org/2004/02/skos/core#>
            PREFIX owl:    <http://www.w3.org/2002/07/owl#>
            PREFIX schema: <https://schema.org/>
            PREFIX phoa:   <http://example.org/phoa#>

            SELECT DISTINCT ?ext
            WHERE {
              <%s> (skos:exactMatch|phoa:exactMatch|owl:sameAs|schema:sameAs) ?ext .
            }
        """.formatted(phobiaUri);

        Dataset ds = store.dataset();
        ds.begin(ReadWrite.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(sparql, ds)) {
            ResultSet rs = qexec.execSelect();
            List<String> out = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution row = rs.nextSolution();
                if (row.contains("ext")) out.add(row.get("ext").toString());
            }
            return out;
        } finally {
            ds.end();
        }
    }

    private void fillFromDbpedia(PhobiaEnrichmentResponse out, String dbpediaResource) throws Exception {
        out.source = "dbpedia";

        String sparql = """
        PREFIX dbo:  <http://dbpedia.org/ontology/>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>

        SELECT (SAMPLE(?label) AS ?label)
               (SAMPLE(?abs)   AS ?abs)
               (SAMPLE(?wiki)  AS ?wiki)
        WHERE {
          VALUES ?s { <%s> }
          OPTIONAL { ?s rdfs:label ?label FILTER (lang(?label) = "en") }
          OPTIONAL { ?s dbo:abstract ?abs FILTER (lang(?abs) = "en") }
          OPTIONAL { ?s foaf:isPrimaryTopicOf ?wiki }
        }
    """.formatted(dbpediaResource);

        JsonNode bindings = sparqlSelectJson(DBPEDIA_SPARQL, sparql, "dbpedia");
        if (bindings.size() == 0) return;

        JsonNode row = bindings.get(0);
        out.label = getLiteral(row, "label");
        out.abstractText = getLiteral(row, "abs");
        out.wikipediaUrl = getValue(row, "wiki");
    }

    private void fillFromWikidata(PhobiaEnrichmentResponse out, String wikidataEntity) throws Exception {
        out.source = "wikidata";

        // Extract Q-id (wd:Qxxxx)
        String qid = wikidataEntity.substring(wikidataEntity.lastIndexOf('/') + 1);

        String sparql = """
    PREFIX wd:      <http://www.wikidata.org/entity/>
    PREFIX schema:  <http://schema.org/>
    PREFIX wikibase:<http://wikiba.se/ontology#>
    PREFIX bd:      <http://www.bigdata.com/rdf#>
    PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>

    SELECT ?label ?desc ?article
    WHERE {
      VALUES ?item { wd:%s }

      SERVICE wikibase:label {
        bd:serviceParam wikibase:language "en" .
        ?item rdfs:label ?label .
      }

      OPTIONAL {
        ?item schema:description ?desc .
        FILTER(LANG(?desc) = "en")
      }

      OPTIONAL {
        ?article schema:about ?item ;
                 schema:isPartOf <https://en.wikipedia.org/> .
      }
    }
    LIMIT 1
""".formatted(qid);


        JsonNode bindings = sparqlSelectJson(WIKIDATA_SPARQL, sparql, "wikidata");
        if (bindings.size() == 0) return;

        JsonNode row = bindings.get(0);
        out.label = getLiteral(row, "label");
        out.description = getLiteral(row, "desc");
        out.wikipediaUrl = getValue(row, "article"); // <-- change var name
    }

    private JsonNode sparqlSelectJson(String endpoint, String sparql, String userAgentTag) throws Exception {
        String url = endpoint + "?query=" + URLEncoder.encode(sparql, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/sparql-results+json")
                .header("User-Agent", "PhoA-WebBackend/1.0 (" + userAgentTag + ")")
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("SPARQL HTTP " + resp.statusCode() + ": " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        return root.path("results").path("bindings");
    }

    private static String getLiteral(JsonNode row, String var) {
        JsonNode n = row.path(var).path("value");
        return n.isMissingNode() ? null : n.asText();
    }

    private static String getValue(JsonNode row, String var) {
        JsonNode n = row.path(var).path("value");
        return n.isMissingNode() ? null : n.asText();
    }
}
