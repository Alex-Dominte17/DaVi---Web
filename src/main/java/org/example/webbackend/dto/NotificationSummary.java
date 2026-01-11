package org.example.webbackend.dto;


import java.util.ArrayList;
import java.util.List;

public class NotificationSummary {
    public String notificationUri;
    public String createdAt;     // schema:dateCreated
    public Double confidence;
    public String status; // "unread" or "read"
    public String detectedPhobia;       // URI
    public String detectedPhobiaLabel;

    public String contextUri;
    public String contextEventName;
    public String contextPlaceName;
    public String contextTime;   // time:inXSDDateTime if present

    public List<InterventionItem> interventions = new ArrayList<>();

    public static class InterventionItem {
        public String iri;
        public String typeIri;
        public String label;
        public String url;

        public InterventionItem(String iri, String typeIri, String label, String url) {
            this.iri = iri;
            this.typeIri = typeIri;
            this.label = label;
            this.url = url;
        }
    }
}

