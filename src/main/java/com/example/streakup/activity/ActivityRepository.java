package com.example.streakup.activity;

import com.example.streakup.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserOrderByDateDescCreatedAtDesc(User user);
    List<Activity> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate from, LocalDate to);
    boolean existsByUserAndDate(User user, LocalDate date);

    @Query("select count(a) from Activity a where a.user = :user and a.date between :from and :to")
    long countByUserAndDateBetween(User user, LocalDate from, LocalDate to);

    @Query("select coalesce(sum(a.durationMinutes), 0) from Activity a where a.user = :user and a.date between :from and :to")
    long durationByUserAndDateBetween(User user, LocalDate from, LocalDate to);
}
