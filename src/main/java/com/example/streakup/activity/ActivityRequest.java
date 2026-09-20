package com.example.streakup.activity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityRequest(
        @NotNull @JsonAlias({"activityType", "category"}) ActivityType type,
        @NotNull @JsonAlias({"activityDate", "performedAt"}) LocalDate date,
        @NotNull @JsonAlias("duration") @Min(1) @Max(1440) Integer durationMinutes,
        @DecimalMin("0.0") @Digits(integer = 6, fraction = 2) BigDecimal distanceKm,
        @Size(max = 500) String notes
) {
}
