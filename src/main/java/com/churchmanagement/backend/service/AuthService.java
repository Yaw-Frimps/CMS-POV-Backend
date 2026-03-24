package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.AuthRequest;
import com.churchmanagement.backend.dto.AuthResponse;
import com.churchmanagement.backend.dto.RegisterRequest;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.model.Role;
import com.churchmanagement.backend.model.User;
import com.churchmanagement.backend.repository.MemberRepository;
import com.churchmanagement.backend.repository.UserRepository;
import com.churchmanagement.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var memberId = memberRepository.findByUserId(user.getId())
                .map(Member::getId)
                .orElse(null);

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .memberId(memberId)
                .build();
    }
}
