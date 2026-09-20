package com.example.streakup.friendship;

import com.example.streakup.user.UserResponse;

import java.time.Instant;

public record FriendshipResponse(
        Long id,
        FriendshipStatus status,
        UserResponse requester,
        UserResponse addressee,
        Instant createdAt,
        Instant updatedAt
) {
    public static FriendshipResponse from(Friendship friendship) {
        return new FriendshipResponse(friendship.getId(), friendship.getStatus(),
                UserResponse.from(friendship.getRequester()), UserResponse.from(friendship.getAddressee()),
                friendship.getCreatedAt(), friendship.getUpdatedAt());
    }
}
