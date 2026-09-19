package com.example.streakup.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username is already in use");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                request.email(),
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}