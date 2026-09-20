package com.example.streakup.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UserAlreadyExistsException("Username is already in use");
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                username,
                email,
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}