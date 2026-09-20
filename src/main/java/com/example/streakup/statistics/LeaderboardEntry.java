package com.example.streakup.statistics;

public record LeaderboardEntry(
        int rank,
        Long userId,
        String username,
        long activities,
        long durationMinutes
) {
}
