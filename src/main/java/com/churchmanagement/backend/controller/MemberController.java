package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.MemberDto;
import com.churchmanagement.backend.dto.ChurchGroupDto;
import com.churchmanagement.backend.dto.UploadResponseDto;
import com.churchmanagement.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDto> createMember(@RequestBody MemberDto memberDto) {
        return ResponseEntity.ok(memberService.createMember(memberDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDto> updateMember(
            @PathVariable Long id,
            @RequestBody MemberDto memberDto) {
        return ResponseEntity.ok(memberService.updateMember(id, memberDto));
    }

    @PostMapping("/{memberId}/profile-image")
    public ResponseEntity<UploadResponseDto> uploadProfileImage(
            @PathVariable Long memberId,
            @RequestParam("profileImage") MultipartFile profileImage,
            @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth
    ) {
        if (profileImage.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(UploadResponseDto.builder()
                            .success(false)
                            .message("No file provided")
                            .uploadedAt(LocalDateTime.now())
                            .build()
                    );
        }

        MemberDto updatedMember = memberService.uploadProfileImage(memberId, profileImage, dateOfBirth);

        UploadResponseDto response = UploadResponseDto.builder()
                .success(true)
                .message("Profile image uploaded successfully")
                .profileImageUrl(updatedMember.getProfileImageUrl())
                .uploadedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<MemberDto> updateMemberProfile(
            @PathVariable Long id,
            @RequestBody MemberDto memberDto) {
        // Find existing member to preserve fields that shouldn't be changed by standard
        // users
        MemberDto existing = memberService.getMemberById(id);
        existing.setDateOfBirth(memberDto.getDateOfBirth());
        existing.setProfileImageUrl(memberDto.getProfileImageUrl());
        if (memberDto.getFirstName() != null)
            existing.setFirstName(memberDto.getFirstName());
        if (memberDto.getLastName() != null)
            existing.setLastName(memberDto.getLastName());
        if (memberDto.getPhone() != null)
            existing.setPhone(memberDto.getPhone());
        if (memberDto.getAddress() != null)
            existing.setAddress(memberDto.getAddress());

        // Map new fields
        existing.setGender(memberDto.getGender());
        existing.setMembershipStatus(memberDto.getMembershipStatus());
        existing.setMaritalStatus(memberDto.getMaritalStatus());
        existing.setEmergencyContact(memberDto.getEmergencyContact());
        existing.setSpouseName(memberDto.getSpouseName());
        existing.setChildrenData(memberDto.getChildrenData());
        existing.setProfession(memberDto.getProfession());

        return ResponseEntity.ok(memberService.updateMember(id, existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/groups")
    public ResponseEntity<List<ChurchGroupDto>> getMemberGroups(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberGroups(id));
    }
}
