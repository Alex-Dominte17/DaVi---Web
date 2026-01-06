package org.example.webbackend.dto;


import java.util.ArrayList;
import java.util.List;

public class PhobiaEnrichmentResponse {
    public String phobiaUri;

    // external URIs found in your RDF graph
    public List<String> externalUris = new ArrayList<>();

    // normalized result (best effort)
    public String source;        // "wikidata" | "dbpedia" | "none"
    public String label;         // english label
    public String description;   // wikidata description (en)
    public String abstractText;  // dbpedia dbo:abstract (en)
    public String wikipediaUrl;  // best effort
}

