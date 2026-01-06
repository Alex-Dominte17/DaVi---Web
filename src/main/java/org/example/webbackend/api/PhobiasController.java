package org.example.webbackend.api;


import org.example.webbackend.service.ExternalKbEnrichmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phobias")
public class PhobiasController {

    private final ExternalKbEnrichmentService enrichmentService;

    public PhobiasController(ExternalKbEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }

    @GetMapping("/{phobiaId}/enrich")
    public ResponseEntity<?> enrich(@PathVariable String phobiaId) {
        return ResponseEntity.ok(enrichmentService.enrichPhobia(phobiaId));
    }
}

