package com.example.streakup.friendship;

import com.example.streakup.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/friends", "/api/friendships"})
public class FriendshipController {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/search")
    public List<UserResponse> search(Authentication authentication,
                                      @RequestParam(name = "q", required = false) String query,
                                      @RequestParam(name = "query", required = false) String alternateQuery) {
        return friendshipService.search(authentication.getName(), query != null ? query : alternateQuery);
    }

    @PostMapping({"/requests", "/request"})
    public FriendshipResponse request(Authentication authentication, @Valid @RequestBody FriendRequest request) {
        return friendshipService.request(authentication.getName(), request.userId());
    }

    @PostMapping("/requests/{userId}")
    public FriendshipResponse requestByPath(Authentication authentication, @PathVariable long userId) {
        return friendshipService.request(authentication.getName(), userId);
    }

    @GetMapping("/requests")
    public List<FriendshipResponse> requests(Authentication authentication) {
        return friendshipService.requests(authentication.getName());
    }

    @PostMapping({"/requests/{requestId}/accept", "/{requestId}/accept"})
    public FriendshipResponse accept(Authentication authentication, @PathVariable long requestId) {
        return friendshipService.accept(authentication.getName(), requestId);
    }

    @GetMapping
    public List<FriendshipResponse> friends(Authentication authentication) {
        return friendshipService.friends(authentication.getName());
    }
}
