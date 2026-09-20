package com.example.streakup.activity;

import com.example.streakup.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activity_user_date", columnList = "user_id, activity_date")
})
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType type;

    @Column(name = "activity_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Activity() {
    }

    public Activity(User user, ActivityType type, LocalDate date, Integer durationMinutes,
                    BigDecimal distanceKm, String notes) {
        this.user = user;
        this.type = type;
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.notes = notes;
        this.createdAt = Instant.now();
    }

    public void update(ActivityType type, LocalDate date, Integer durationMinutes,
                       BigDecimal distanceKm, String notes) {
        this.type = type;
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public ActivityType getType() { return type; }
    public LocalDate getDate() { return date; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
