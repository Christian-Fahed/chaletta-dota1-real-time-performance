package com.chaletta.chalettaperformance.config;

import com.chaletta.chalettaperformance.model.User;
import com.chaletta.chalettaperformance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    @Value("${app.default-user.username}")
    private String defaultUsername;

    @Value("${app.default-user.password}")
    private String defaultPassword;

    @Value("${app.default-user.role}")
    private String defaultRole;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(defaultUsername).isEmpty()) {
            User user = new User();
            user.setUsername(defaultUsername);
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setRole(User.Role.valueOf(defaultRole));
            userRepository.save(user);
            log.info("Default user '{}' created with role {}.", defaultUsername, defaultRole);
        } else {
            log.info("Default user '{}' already exists, skipping.", defaultUsername);
        }
    }
}