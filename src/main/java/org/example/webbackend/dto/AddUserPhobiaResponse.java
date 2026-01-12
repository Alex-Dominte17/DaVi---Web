package org.example.webbackend.dto;


public class AddUserPhobiaResponse {
    public String userUri;
    public String phobiaUri;
    public String wikidataUri;
    public String label;

    public AddUserPhobiaResponse() {}
    public AddUserPhobiaResponse(String userUri, String phobiaUri, String wikidataUri, String label) {
        this.userUri = userUri;
        this.phobiaUri = phobiaUri;
        this.wikidataUri = wikidataUri;
        this.label = label;
    }
}
