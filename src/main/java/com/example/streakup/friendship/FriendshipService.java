package com.example.streakup.friendship;

import com.example.streakup.user.User;
import com.example.streakup.user.UserRepository;
import com.example.streakup.user.UserResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> search(String username, String query) {
        User current = user(username);
        String value = query == null ? "" : query.trim();
        if (value.isBlank()) {
            return List.of();
        }
        return userRepository.searchByUsernameOrEmail(value, current.getId(), PageRequest.of(0, 20))
                .stream().map(UserResponse::from).toList();
    }

    @Transactional
    public FriendshipResponse request(String username, long userId) {
        User requester = user(username);
        User addressee = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (requester.getId().equals(addressee.getId())) {
            throw new FriendshipException(HttpStatus.BAD_REQUEST, "You cannot befriend yourself");
        }
        var existing = friendshipRepository.findBetween(requester, addressee);
        if (existing.isPresent()) {
            Friendship friendship = existing.get();
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new FriendshipException(HttpStatus.CONFLICT, "Users are already friends");
            }
            if (friendship.getRequester().getId().equals(addressee.getId())) {
                friendship.accept();
                return FriendshipResponse.from(friendship);
            }
            throw new FriendshipException(HttpStatus.CONFLICT, "Friend request already exists");
        }
        return FriendshipResponse.from(friendshipRepository.save(new Friendship(requester, addressee)));
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> requests(String username) {
        return friendshipRepository.findByAddresseeAndStatusOrderByCreatedAtDesc(
                        user(username), FriendshipStatus.PENDING)
                .stream().map(FriendshipResponse::from).toList();
    }

    @Transactional
    public FriendshipResponse accept(String username, long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Friend request not found"));
        if (!friendship.getAddressee().getUsername().equals(username)) {
            throw new FriendshipException(HttpStatus.FORBIDDEN, "Only the recipient can accept this request");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new FriendshipException(HttpStatus.CONFLICT, "Friend request is not pending");
        }
        friendship.accept();
        return FriendshipResponse.from(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> friends(String username) {
        return friendshipRepository.findForUser(user(username), FriendshipStatus.ACCEPTED)
                .stream().map(FriendshipResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public boolean areAcceptedFriends(String username, long userId) {
        User current = user(username);
        User other = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return friendshipRepository.findBetween(current, other)
                .map(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    private User user(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
