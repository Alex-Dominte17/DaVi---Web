package org.example.webbackend.dto;


public class LatestContextResponse {
    public String contextUri;

    public String eventName;
    public String placeName;
    public Boolean addressed;

    public Double lat;
    public Double lon;

    public String seasonLabel;
    public String time;        // ISO dateTime string
    public String sourceLabel; // e.g. PhoneGPS
}
