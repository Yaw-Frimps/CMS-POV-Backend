package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.AuthRequest;
import com.churchmanagement.backend.dto.AuthResponse;
import com.churchmanagement.backend.dto.RegisterRequest;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.model.Role;
import com.churchmanagement.backend.model.User;
import com.churchmanagement.backend.repository.MemberRepository;
import com.churchmanagement.backend.repository.UserRepository;
import com.churchmanagement.backend.repository.PasswordResetTokenRepository;
import com.churchmanagement.backend.model.PasswordResetToken;
import com.churchmanagement.backend.security.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.isAdmin() ? Role.ADMIN : Role.USER)
                .build();

        var savedUser = userRepository.save(user);

        // Auto-create a member profile
        var member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .user(savedUser)
                .build();

        var savedMember = memberRepository.save(member);

        var jwtToken = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .memberId(savedMember.getId())
                .profileImageUrl(savedMember.getProfileImageUrl())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var member = memberRepository.findByUserId(user.getId()).orElse(null);
        var memberId = member != null ? member.getId() : null;
        var profileImageUrl = member != null ? member.getProfileImageUrl() : null;

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .memberId(memberId)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    @Transactional
    public void forgotPassword(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Delete existing tokens for this user
        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        tokenRepository.save(resetToken);

        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        
        String userName = user.getEmail().split("@")[0];
        Member member = memberRepository.findByUserId(user.getId()).orElse(null);
        if (member != null) {
            userName = member.getFirstName();
        }

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Password Reset Request",
                "emails/password-reset",
                Map.of("userName", userName, "resetUrl", resetUrl)
        );
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
