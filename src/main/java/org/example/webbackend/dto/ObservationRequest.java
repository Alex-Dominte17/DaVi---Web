package org.example.webbackend.dto;


public class ObservationRequest {
    public String userId;
    public String observationId;

    public String sensor;           // e.g. "AppleWatchHeartRateSensor"
    public String observedProperty; // e.g. "https://schema.org/heartRate" or "heartRate"
    public String value;            // keep as string; we will store as typed literal if possible
    public String unit;             // optional, e.g. "bpm"

    public String time;             // ISO-8601, e.g. "2026-01-05T09:05:10Z"
    public String sourceName;       // e.g. "AppleWatch"
}

