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

    private final AppProperties appProperties;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void run(String... args) throws Exception {
        String adminEmail = appProperties.getDefaultAdmin().getEmail();
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Default admin not found. Creating default admin account...");
            
            // Create User
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(appProperties.getDefaultAdmin().getPassword()))
                    .role(Role.ADMIN)
                    .build();
            
            User savedUser = userRepository.save(adminUser);
            
            // Create Member Profile
            Member adminProfile = Member.builder()
                    .firstName(appProperties.getDefaultAdmin().getFirstName())
                    .lastName(appProperties.getDefaultAdmin().getLastName())
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
