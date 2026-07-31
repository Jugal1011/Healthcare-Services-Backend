package com.hospital.management.config;

import com.hospital.management.entity.Role;
import com.hospital.management.entity.User;
import com.hospital.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default ADMIN account on first boot so there's a way in before any admin exists.
 * CHANGE THIS PASSWORD IMMEDIATELY after first login in any real deployment,
 * and consider removing this seeder entirely once you have a real admin provisioning flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@hospital.com")) {
            return;
        }

        User admin = User.builder()
                .name("Default Admin")
                .email("admin@hospital.com")
                .password(passwordEncoder.encode("Admin@123"))
                .mobileNumber("9999999999")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.warn("Seeded default admin account -> email: admin@hospital.com / password: Admin@123. CHANGE THIS PASSWORD.");
    }
}
