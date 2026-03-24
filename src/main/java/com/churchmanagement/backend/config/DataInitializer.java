package com.churchmanagement.backend.config;

import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.model.Role;
import com.churchmanagement.backend.model.User;
import com.churchmanagement.backend.repository.MemberRepository;
import com.churchmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.default-admin.email}")
    private String adminEmail;

    @org.springframework.beans.factory.annotation.Value("${app.default-admin.password}")
    private String adminPassword;

    @org.springframework.beans.factory.annotation.Value("${app.default-admin.first-name}")
    private String adminFirstName;

    @org.springframework.beans.factory.annotation.Value("${app.default-admin.last-name}")
    private String adminLastName;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Default admin not found. Creating default admin account...");
            
            // Create User
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            
            User savedUser = userRepository.save(adminUser);
            
            // Create Member Profile
            Member adminProfile = Member.builder()
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .gender("Other")
                    .membershipStatus("Leader")
                    .user(savedUser)
                    .build();
            
            memberRepository.save(adminProfile);
            
            log.info("Default admin account created successfully.");
        } else {
            log.info("Default admin account already exists.");
        }
    }
}
