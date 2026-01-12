package org.example.webbackend.api;


import org.example.webbackend.dto.PhobiaSuggestItem;
import org.example.webbackend.service.PhobiaSuggestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phobias")
public class PhobiaSuggestionController {

    private final PhobiaSuggestionService service;

    public PhobiaSuggestionController(PhobiaSuggestionService service) {
        this.service = service;
    }

    @GetMapping("/random")
    public List<PhobiaSuggestItem> random(
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit
    ) {
        return service.randomPhobias(limit);
    }
}

