package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.MemberDto;
import com.churchmanagement.backend.dto.ChurchGroupDto;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;
    private static final  String MEMBERNOTFOUND = "Member not found";

    @org.springframework.beans.factory.annotation.Value("${app.default-admin.email}")
    private String adminEmail;

    public List<MemberDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public MemberDto getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException(MEMBERNOTFOUND));
    }

    @Transactional
    public MemberDto createMember(MemberDto memberDto) {
        Member member = Member.builder()
                .firstName(memberDto.getFirstName())
                .lastName(memberDto.getLastName())
                .phone(memberDto.getPhone())
                .address(memberDto.getAddress())
                .dateOfBirth(memberDto.getDateOfBirth())
                .profileImageUrl(memberDto.getProfileImageUrl())
                .joinedDate(memberDto.getJoinedDate() != null ? memberDto.getJoinedDate() : java.time.LocalDate.now())
                .build();
        return mapToDto(memberRepository.save(member));
    }

    @Transactional
    public MemberDto uploadProfileImage(Long memberId, MultipartFile file, LocalDate dateOfBirth) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException(MEMBERNOTFOUND));

        // Protection for default admin
        if (member.getUser() != null && adminEmail.equalsIgnoreCase(member.getUser().getEmail())) {
            throw new RuntimeException("Default admin details cannot be updated through the dashboard.");
        }

        String oldFileName = null;
        if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isBlank()) {
            oldFileName = Paths.get(member.getProfileImageUrl()).getFileName().toString();
        }

        // Store file
        String storedFileName = fileStorageService.storeFile(file, "members", memberId.toString(), oldFileName);
        String fileUrl = fileStorageService.getFileUrl("members", memberId.toString(), storedFileName);

        // Update member
        member.setProfileImageUrl(fileUrl);
        member.setProfileImageFilename(file.getOriginalFilename());
        member.setProfileImageUploadedAt(LocalDateTime.now());
        if (dateOfBirth != null) {
            member.setDateOfBirth(dateOfBirth);
        }

        memberRepository.save(member);

        return mapToDto(member);
    }

    @Transactional
    public MemberDto updateMember(Long id, MemberDto memberDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MEMBERNOTFOUND));

        // Protection for default admin
        if (member.getUser() != null && adminEmail.equalsIgnoreCase(member.getUser().getEmail())) {
            throw new RuntimeException("Default admin details cannot be updated through the dashboard.");
        }

        member.setFirstName(memberDto.getFirstName());
        member.setLastName(memberDto.getLastName());
        member.setPhone(memberDto.getPhone());
        member.setAddress(memberDto.getAddress());
        member.setDateOfBirth(memberDto.getDateOfBirth());
        member.setProfileImageUrl(memberDto.getProfileImageUrl());
        member.setJoinedDate(memberDto.getJoinedDate());
        member.setGender(memberDto.getGender());
        member.setMembershipStatus(memberDto.getMembershipStatus());
        member.setMaritalStatus(memberDto.getMaritalStatus());
        member.setEmergencyContact(memberDto.getEmergencyContact());
        member.setSpouseName(memberDto.getSpouseName());
        member.setChildrenData(memberDto.getChildrenData());
        member.setProfession(memberDto.getProfession());

        return mapToDto(memberRepository.save(member));
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public List<ChurchGroupDto> getMemberGroups(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MEMBERNOTFOUND));
        return member.getGroups().stream()
                .map(group -> ChurchGroupDto.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .description(group.getDescription())
                        .meetingSchedule(group.getMeetingSchedule())
                        .memberCount(group.getMembers() != null ? group.getMembers().size() : 0)
                        .category(group.getCategory() != null ? group.getCategory() : "General")
                        .build())
                .toList();
    }

    private MemberDto mapToDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .phone(member.getPhone())
                .address(member.getAddress())
                .dateOfBirth(member.getDateOfBirth())
                .profileImageUrl(member.getProfileImageUrl())
                .joinedDate(member.getJoinedDate())
                .userId(member.getUser() != null ? member.getUser().getId() : null)
                .email(member.getUser() != null ? member.getUser().getEmail() : null)
                .accountCreatedAt(member.getUser() != null ? member.getUser().getCreatedAt() : null)
                .groupNames(member.getGroups() != null 
                    ? member.getGroups().stream().map(com.churchmanagement.backend.model.ChurchGroup::getName).toList() 
                    : java.util.Collections.emptyList())
                .gender(member.getGender())
                .membershipStatus(member.getMembershipStatus())
                .maritalStatus(member.getMaritalStatus())
                .emergencyContact(member.getEmergencyContact())
                .spouseName(member.getSpouseName())
                .childrenData(member.getChildrenData())
                .profession(member.getProfession())
                .build();
    }
}
