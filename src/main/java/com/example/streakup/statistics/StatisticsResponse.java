package com.example.streakup.statistics;

public record StatisticsResponse(
        Long userId,
        String username,
        long currentStreak,
        long longestStreak,
        long totalActivities,
        long totalDurationMinutes,
        long weeklyActivities,
        long weeklyDurationMinutes
) {
}
