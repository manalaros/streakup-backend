package com.example.streakup.friendship;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record FriendRequest(@NotNull @JsonAlias({"friendId", "targetUserId"}) Long userId) {
}
