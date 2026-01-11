package org.example.webbackend.api.error;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShaclValidationException.class)
    public ResponseEntity<?> handleShacl(ShaclValidationException ex) {
        // 422 is common for "syntactically valid but semantically invalid"
        return ResponseEntity.unprocessableEntity().body(Map.of(
                "error", ex.getMessage(),
                "shaclReportTurtle", ex.getReportTurtle()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}

