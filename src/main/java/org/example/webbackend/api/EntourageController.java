package org.example.webbackend.api;


import org.example.webbackend.dto.EntourageContactPatchRequest;
import org.example.webbackend.dto.EntourageContactRequest;
import org.example.webbackend.service.EntourageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/entourage")
public class EntourageController {

    private final EntourageService entourageService;

    public EntourageController(EntourageService entourageService) {
        this.entourageService = entourageService;
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable String userId) {
        return ResponseEntity.ok(entourageService.listEntourage(userId));
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable String userId, @RequestBody EntourageContactRequest req) {
        return ResponseEntity.ok(entourageService.addContact(userId, req));
    }

    @PatchMapping("/{contactId}")
    public ResponseEntity<?> patch(@PathVariable String userId,
                                   @PathVariable String contactId,
                                   @RequestBody EntourageContactPatchRequest req) {
        boolean ok = entourageService.patchContact(userId, contactId, req);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("contactId", contactId, "updated", true));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<?> remove(@PathVariable String userId, @PathVariable String contactId) {
        boolean existed = entourageService.removeContact(userId, contactId);
        if (!existed) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("contactId", contactId, "deleted", true));
    }
}
