package org.example.webbackend.dto;


public class ContextRequest {
    public String userId;
    public String contextId;

    public String eventName;
    public String placeName;

    public Double lat;
    public Double lon;

    public String time;     // ISO-8601, e.g. "2026-01-05T09:05:00Z"
    public String season;   // e.g. "Winter"
    public String sourceName; // e.g. "PhoneGPS"
}

