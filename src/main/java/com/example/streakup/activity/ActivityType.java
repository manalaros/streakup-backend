package com.example.streakup.activity;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum ActivityType {
    WALKING,
    RUNNING,
    CYCLING,
    SWIMMING,
    GYM,
    STRENGTH,
    YOGA,
    MEDITATION,
    STUDY,
    READING,
    OTHER;

    @JsonCreator
    public static ActivityType from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported activity type: " + value);
        }
    }
}
