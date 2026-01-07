package org.example.webbackend.dto;

public class EntourageContactRequest {
    public String contactId;      // optional; if null we auto-generate
    public String name;           // required
    public String email;          // optional
    public String relationship;   // e.g. "Family", "Friend", "Professional"
    public Boolean alertsEnabled; // optional (default true)
}
