package com.example.streakup.friendship;

import org.springframework.http.HttpStatus;

public class FriendshipException extends RuntimeException {
    private final HttpStatus status;

    public FriendshipException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
