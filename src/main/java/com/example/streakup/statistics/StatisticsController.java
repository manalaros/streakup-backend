package com.example.streakup.statistics;

import com.example.streakup.activity.ActivityResponse;
import com.example.streakup.activity.ActivityService;
import com.example.streakup.friendship.FriendshipService;
import com.example.streakup.user.UserRepository;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final ActivityService activityService;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;

    public StatisticsController(StatisticsService statisticsService, ActivityService activityService,
                                UserRepository userRepository, FriendshipService friendshipService) {
        this.statisticsService = statisticsService;
        this.activityService = activityService;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
    }

    @GetMapping({"/api/statistics", "/api/stats", "/api/streak", "/api/streaks"})
    public StatisticsResponse own(Authentication authentication) {
        return statisticsService.own(authentication.getName());
    }

    @GetMapping({"/api/statistics/{userId}", "/api/users/{userId}/statistics"})
    public StatisticsResponse byUser(Authentication authentication, @PathVariable @Min(1) long userId) {
        return statisticsService.visible(authentication.getName(), userId);
    }

    @GetMapping("/api/users/{userId}/activities")
    public List<ActivityResponse> activities(Authentication authentication, @PathVariable @Min(1) long userId) {
        var target = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        var current = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!current.getId().equals(target.getId()) && !friendshipService.areAcceptedFriends(authentication.getName(), userId)) {
            throw new AccessDeniedException("Activities are visible to accepted friends only");
        }
        return activityService.activitiesFor(target);
    }

    @GetMapping({"/api/leaderboard/weekly", "/api/leaderboard"})
    public List<LeaderboardEntry> leaderboard(Authentication authentication) {
        return statisticsService.weeklyLeaderboard(authentication.getName());
    }
}
