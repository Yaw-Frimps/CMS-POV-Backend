package com.churchmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate joinedDate;
    private String profileImageUrl;
    private Long userId;
    
    // New fields
    private String email;
    private LocalDateTime accountCreatedAt;
    private List<String> groupNames;

    // New profile fields
    private String gender;
    private String membershipStatus;
    private String maritalStatus;
    private String emergencyContact;
    private String spouseName;
    private String childrenData;
    private String profession;
}
