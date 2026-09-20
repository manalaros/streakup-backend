package com.example.streakup.statistics;

import com.example.streakup.activity.Activity;
import com.example.streakup.activity.ActivityRepository;
import com.example.streakup.friendship.Friendship;
import com.example.streakup.friendship.FriendshipRepository;
import com.example.streakup.friendship.FriendshipStatus;
import com.example.streakup.user.User;
import com.example.streakup.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class StatisticsService {
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final FriendshipRepository friendshipRepository;
    private final ZoneId zoneId;

    public StatisticsService(UserRepository userRepository, ActivityRepository activityRepository,
                             FriendshipRepository friendshipRepository,
                             @Value("${app.timezone:UTC}") String timezone) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.friendshipRepository = friendshipRepository;
        this.zoneId = ZoneId.of(timezone);
    }

    @Transactional(readOnly = true)
    public StatisticsResponse own(String username) {
        return forUser(user(username));
    }

    @Transactional(readOnly = true)
    public StatisticsResponse visible(String requester, long userId) {
        User current = user(requester);
        User target = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!current.getId().equals(target.getId()) && !areFriends(current, target)) {
            throw new org.springframework.security.access.AccessDeniedException("Statistics are visible to accepted friends only");
        }
        return forUser(target);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> weeklyLeaderboard(String username) {
        LocalDate today = LocalDate.now(zoneId);
        LocalDate from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = from.plusDays(6);
        List<LeaderboardEntry> sorted = visibleUsers(user(username)).stream()
                .map(user -> {
                    List<Activity> activities = activityRepository.findByUserAndDateBetweenOrderByDateAsc(user, from, to);
                    return new LeaderboardEntry(0, user.getId(), user.getUsername(), activities.size(),
                            activities.stream().mapToLong(Activity::getDurationMinutes).sum());
                })
                .sorted(Comparator.comparingLong(LeaderboardEntry::activities).reversed()
                        .thenComparing(Comparator.comparingLong(LeaderboardEntry::durationMinutes).reversed())
                        .thenComparing(LeaderboardEntry::username))
                .toList();
        List<LeaderboardEntry> ranked = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            LeaderboardEntry entry = sorted.get(index);
            ranked.add(new LeaderboardEntry(index + 1, entry.userId(), entry.username(),
                    entry.activities(), entry.durationMinutes()));
        }
        return ranked;
    }

    private StatisticsResponse forUser(User user) {
        LocalDate today = LocalDate.now(zoneId);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Activity> activities = activityRepository.findByUserOrderByDateDescCreatedAtDesc(user);
        Set<LocalDate> dates = activities.stream().map(Activity::getDate)
                .filter(date -> !date.isAfter(today)).collect(java.util.stream.Collectors.toSet());
        long current = 0;
        LocalDate cursor = today;
        while (dates.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }
        long longest = 0;
        long run = 0;
        for (LocalDate date = dates.stream().min(LocalDate::compareTo).orElse(today);
             !date.isAfter(today); date = date.plusDays(1)) {
            if (dates.contains(date)) {
                run++;
                longest = Math.max(longest, run);
            } else {
                run = 0;
            }
        }
        long weeklyActivities = activities.stream().filter(a -> !a.getDate().isBefore(weekStart)
                && !a.getDate().isAfter(weekStart.plusDays(6))).count();
        long weeklyDuration = activities.stream().filter(a -> !a.getDate().isBefore(weekStart)
                && !a.getDate().isAfter(weekStart.plusDays(6)))
                .mapToLong(Activity::getDurationMinutes).sum();
        return new StatisticsResponse(user.getId(), user.getUsername(), current, longest, activities.size(),
                activities.stream().mapToLong(Activity::getDurationMinutes).sum(), weeklyActivities, weeklyDuration);
    }

    private List<User> visibleUsers(User current) {
        List<User> result = new ArrayList<>();
        result.add(current);
        for (Friendship friendship : friendshipRepository.findForUser(current, FriendshipStatus.ACCEPTED)) {
            result.add(friendship.getRequester().getId().equals(current.getId())
                    ? friendship.getAddressee() : friendship.getRequester());
        }
        return result;
    }

    private boolean areFriends(User first, User second) {
        return friendshipRepository.findBetween(first, second)
                .map(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED).orElse(false);
    }

    private User user(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
