package org.example.webbackend.api;


import org.example.webbackend.dto.AckRequest;
import org.example.webbackend.service.NotificationCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsAckController {

    private final NotificationCommandService commandService;

    public NotificationsAckController(NotificationCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<?> ack(@PathVariable String id,
                                 @RequestBody(required = false) AckRequest req) {
        boolean ok = commandService.ackNotification(id, req);
        if (!ok) return ResponseEntity.notFound().build();
        String status = (req != null && req.status != null && !req.status.isBlank()) ? req.status : "read";
        return ResponseEntity.ok(Map.of("id", id, "status", status));
    }
}
