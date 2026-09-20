package com.example.streakup.activity;

import com.example.streakup.user.User;
import com.example.streakup.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> ownActivities(String username) {
        return activitiesFor(user(username));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> activitiesFor(User user) {
        return activityRepository.findByUserOrderByDateDescCreatedAtDesc(user).stream()
                .map(ActivityResponse::from).toList();
    }

    @Transactional
    public ActivityResponse create(String username, ActivityRequest request) {
        User user = user(username);
        Activity saved = activityRepository.save(new Activity(user, request.type(), request.date(),
                request.durationMinutes(), request.distanceKm(), request.notes()));
        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse update(String username, long id, ActivityRequest request) {
        Activity activity = owned(username, id);
        activity.update(request.type(), request.date(), request.durationMinutes(),
                request.distanceKm(), request.notes());
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void delete(String username, long id) {
        activityRepository.delete(owned(username, id));
    }

    @Transactional(readOnly = true)
    public Activity owned(String username, long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));
        if (!activity.getUser().getUsername().equals(username)) {
            throw new NoSuchElementException("Activity not found");
        }
        return activity;
    }

    private User user(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
