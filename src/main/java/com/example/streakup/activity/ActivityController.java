package com.example.streakup.activity;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityResponse> list(Authentication authentication) {
        return activityService.ownActivities(authentication.getName());
    }

    @GetMapping("/{id}")
    public ActivityResponse get(Authentication authentication, @PathVariable long id) {
        return ActivityResponse.from(activityService.owned(authentication.getName(), id));
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> create(Authentication authentication,
                                                    @Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.create(authentication.getName(), request);
        return ResponseEntity.created(URI.create("/api/activities/" + response.id())).body(response);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ActivityResponse update(Authentication authentication, @PathVariable long id,
                                   @Valid @RequestBody ActivityRequest request) {
        return activityService.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable long id) {
        activityService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
