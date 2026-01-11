package org.example.webbackend.api;

import org.example.webbackend.dto.EvaluateRequest;
import org.example.webbackend.dto.LatestContextResponse;
import org.example.webbackend.dto.LatestObservationResponse;
import org.example.webbackend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final PhobiaQueryService phobiaQueryService;
    private final ContextQueryService contextQueryService;
    private final ObservationQueryService observationQueryService;
    private final EvaluationService evaluationService;
    private final NotificationQueryService notificationQueryService;




    public UsersController(
            PhobiaQueryService phobiaQueryService,
            ContextQueryService contextQueryService,
            ObservationQueryService observationQueryService,
            EvaluationService evaluationService,
            NotificationQueryService notificationQueryService
    ) {
        this.phobiaQueryService = phobiaQueryService;
        this.contextQueryService = contextQueryService;
        this.observationQueryService = observationQueryService;
        this.evaluationService = evaluationService;
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/{userLocalName}/phobias")
    public List<Map<String, Object>> getUserPhobias(@PathVariable String userLocalName) {
        return phobiaQueryService.getUserPhobias(userLocalName);
    }

    @GetMapping("/{userLocalName}/context/latest")
    public ResponseEntity<?> getLatestContext(@PathVariable String userLocalName) {
        LatestContextResponse latest = contextQueryService.getLatestContext(userLocalName);
        if (latest == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(latest);
    }
    @GetMapping("/{userLocalName}/observations/latest")
    public ResponseEntity<?> getLatestObservation(
            @PathVariable String userLocalName,
            @RequestParam String property
    ) {
        LatestObservationResponse latest =
                observationQueryService.getLatestObservation(userLocalName, property);

        if (latest == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(latest);
    }

    @PostMapping("/{userLocalName}/evaluate")
    public ResponseEntity<?> evaluate(@PathVariable String userLocalName,
                                      @RequestBody(required = false) EvaluateRequest req) {
        if (req == null) req = new EvaluateRequest();
        List<String> notifUris = evaluationService.evaluateUserAllContexts(userLocalName, req, 20);
        if (notifUris.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(Map.of("notificationUris", notifUris));
    }

    @GetMapping("/{userLocalName}/notifications")
    public ResponseEntity<?> listNotifications(
            @PathVariable String userLocalName,
            @RequestParam(required = false, defaultValue = "200") int limit
    ) {
        return ResponseEntity.ok(notificationQueryService.listUserNotifications(userLocalName, limit));
    }
}
