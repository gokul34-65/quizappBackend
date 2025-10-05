package com.saanya.quiz_app.service;

import com.saanya.quiz_app.dto.LoginRequest;
import com.saanya.quiz_app.dto.RegisterRequest;
import com.saanya.quiz_app.dto.UserResponse;
import com.saanya.quiz_app.model.User;
import com.saanya.quiz_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        // 1. Check if username already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 2. Create new user
        User user = new User();
        user.setUsername(request.getUsername());

        // 3. Hash password (NEVER store plain text!)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // "pass123" becomes something like "$2a$10$xYz..."

        user.setHighestStreak(0);

        // 4. Save to database
        User savedUser = userRepository.save(user);

        // 5. Return response (without password!)
        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getHighestStreak()
        );
    }

    public UserResponse login(LoginRequest request) {
        // 1. Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // 2. Compare hashed passwords
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // passwordEncoder.matches("pass123", "$2a$10$xYz...") → true/false
            throw new RuntimeException("Invalid username or password");
        }

        // 3. Return user data
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getHighestStreak()
        );
    }
}
