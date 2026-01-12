package org.example.webbackend.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webbackend.dto.PhobiaSuggestItem;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


@Service
public class PhobiaSuggestionService {

    private static final String WIKIDATA_SPARQL = "https://query.wikidata.org/sparql";
    private static final String PHOBIA_QID = "Q175854"; // phobia

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // existing suggest(q, limit) ... keep it


    public List<PhobiaSuggestItem> randomPhobias(int limit) {
        long seed=17;
        int lim = Math.max(1, Math.min(limit <= 0 ? 20 : limit, 50));

        int fetch = Math.max(100, lim * 10);
        fetch = Math.min(fetch, 500);

        String sparql = """
            PREFIX wd:       <http://www.wikidata.org/entity/>
            PREFIX wdt:      <http://www.wikidata.org/prop/direct/>
            PREFIX bd:       <http://www.bigdata.com/rdf#>
            PREFIX schema:   <http://schema.org/>

            SELECT ?item ?itemLabel ?desc
            WHERE {
              {
                ?item wdt:P279* wd:%s .
              } UNION {
                ?item wdt:P31 / wdt:P279* wd:%s .
              }

              OPTIONAL {
                ?item schema:description ?desc .
                FILTER(LANG(?desc) = "en")
              }

              SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }

              FILTER(
                REGEX(LCASE(STR(?itemLabel)), "phobia$") ||
                (BOUND(?desc) && REGEX(LCASE(STR(?desc)), "fear"))
              )
            }
            LIMIT %d
            """.formatted(PHOBIA_QID, PHOBIA_QID, fetch);

        try {
            JsonNode bindings = sparqlSelectJson(WIKIDATA_SPARQL, sparql);

            List<PhobiaSuggestItem> all = new ArrayList<>();
            for (JsonNode row : bindings) {
                String uri = getValue(row, "item");
                String label = getValue(row, "itemLabel");
                String desc = getValue(row, "desc");
                if (uri != null && label != null) {
                    all.add(new PhobiaSuggestItem(uri, label, desc));
                }
            }

            Collections.shuffle(all, new Random(seed));

            if (all.size() > lim) return all.subList(0, lim);
            return all;
        } catch (Exception e) {
            return List.of();
        }
    }

    private JsonNode sparqlSelectJson(String endpoint, String sparql) throws Exception {
        String url = endpoint + "?query=" + URLEncoder.encode(sparql, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/sparql-results+json")
                .header("User-Agent", "PhoA-WebBackend/1.0 (phobia-random)")
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("SPARQL HTTP " + resp.statusCode() + ": " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        return root.path("results").path("bindings");
    }

    private static String getValue(JsonNode row, String var) {
        JsonNode n = row.path(var).path("value");
        return n.isMissingNode() ? null : n.asText();
    }
}

