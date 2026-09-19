package com.example.streakup.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        UserResponse response = userService.register(request);

        URI location = URI.create("/api/users/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}