package org.example.webbackend.api;


import org.example.webbackend.dto.ContextRequest;
import org.example.webbackend.dto.ObservationRequest;
import org.example.webbackend.service.IngestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class IngestController {

    private final IngestService ingestService;

    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/context")
    public ResponseEntity<?> postContext(@RequestBody ContextRequest req) {
        String uri = ingestService.ingestContext(req);
        return ResponseEntity.ok(Map.of("uri", uri));
    }

    @PostMapping("/observations")
    public ResponseEntity<?> postObservation(@RequestBody ObservationRequest req) {
        String uri = ingestService.ingestObservation(req);
        return ResponseEntity.ok(Map.of("uri", uri));
    }
}

