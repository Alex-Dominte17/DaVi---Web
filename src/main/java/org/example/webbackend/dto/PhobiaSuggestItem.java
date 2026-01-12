package org.example.webbackend.dto;

public class PhobiaSuggestItem {
    public String wikidataUri;
    public String label;
    public String description;

    public PhobiaSuggestItem() {}
    public PhobiaSuggestItem(String wikidataUri, String label, String description) {
        this.wikidataUri = wikidataUri;
        this.label = label;
        this.description = description;
    }
}

