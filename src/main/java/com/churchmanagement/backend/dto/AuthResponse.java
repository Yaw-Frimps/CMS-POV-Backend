package com.churchmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private Long memberId; // Reference to the user's member profile id
    private String profileImageUrl;
    private boolean profileComplete;
    private boolean profileLinked; // true when signup linked to a pre-registered record
}
