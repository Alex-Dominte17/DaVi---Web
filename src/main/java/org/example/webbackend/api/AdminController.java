package org.example.webbackend.api;

import org.example.webbackend.rdf.RdfStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RdfStore store;
    private final String resetToken;

    public AdminController(RdfStore store,
                           @Value("${phoa.admin.reset-token}") String resetToken) {
        this.store = store;
        this.resetToken = resetToken;
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestHeader("X-Admin-Token") String token) {
        if (!resetToken.equals(token)) return ResponseEntity.status(403).body("Forbidden");
        store.resetToInitialModel();
        return ResponseEntity.ok("Reset done");
    }
}
