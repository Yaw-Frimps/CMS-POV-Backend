package com.churchmanagement.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    private LocalDate dateOfBirth;
    private LocalDate joinedDate;
    private String profileImageUrl;
    // Add these fields
    private String profileImageFilename; // original filename
    private LocalDateTime profileImageUploadedAt; // optional, tracks upload time

    private String gender;
    private String membershipStatus; // member, worker, leader
    private String maritalStatus; // Single, Married
    private String emergencyContact;
    private String spouseName;
    private String childrenData; // JSON string for names and ages
    private String profession;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    @Builder.Default
    private List<ChurchGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Donation> donations = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Attendance> attendanceRecords = new ArrayList<>();

}
