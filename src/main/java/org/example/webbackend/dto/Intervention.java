package org.example.webbackend.dto;

public class Intervention {

    public String iri;
    public String typeIri;
    public String label;
    public String url;

    public Intervention(String iri, String typeIri, String label, String url) {
        this.iri = iri;
        this.typeIri = typeIri;
        this.label = label;
        this.url = url;
    }
}