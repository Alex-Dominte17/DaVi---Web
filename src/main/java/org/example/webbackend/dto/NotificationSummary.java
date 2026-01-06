package org.example.webbackend.dto;


import java.util.ArrayList;
import java.util.List;

public class NotificationSummary {
    public String notificationUri;
    public String createdAt;     // schema:dateCreated
    public Double confidence;

    public String contextUri;
    public String contextEventName;
    public String contextPlaceName;
    public String contextTime;   // time:inXSDDateTime if present

    public List<InterventionItem> interventions = new ArrayList<>();

    public static class InterventionItem {
        public String uri;
        public String label;

        public InterventionItem() {}
        public InterventionItem(String uri, String label) {
            this.uri = uri;
            this.label = label;
        }
    }
}

