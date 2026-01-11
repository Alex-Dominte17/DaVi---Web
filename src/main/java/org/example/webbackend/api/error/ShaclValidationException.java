package org.example.webbackend.api.error;


public class ShaclValidationException extends RuntimeException {
    private final String reportTurtle;

    public ShaclValidationException(String message, String reportTurtle) {
        super(message);
        this.reportTurtle = reportTurtle;
    }

    public String getReportTurtle() {
        return reportTurtle;
    }
}

