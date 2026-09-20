package com.example.streakup.activity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ActivityResponse(
        Long id,
        Long userId,
        String username,
        ActivityType type,
        LocalDate date,
        Integer durationMinutes,
        BigDecimal distanceKm,
        String notes,
        Instant createdAt
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getUser().getId(), activity.getUser().getUsername(),
                activity.getType(), activity.getDate(), activity.getDurationMinutes(), activity.getDistanceKm(),
                activity.getNotes(), activity.getCreatedAt());
    }
}
