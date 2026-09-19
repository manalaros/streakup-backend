package com.example.streakup.auth;

import com.example.streakup.user.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}