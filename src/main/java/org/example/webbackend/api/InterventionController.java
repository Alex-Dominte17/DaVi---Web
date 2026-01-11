package org.example.webbackend.api;

import org.example.webbackend.dto.Intervention;
import org.example.webbackend.service.InterventionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @GetMapping("/interventions")
    public List<Intervention> getAllInterventions() {
        return interventionService.listAllInterventions();
    }
}

