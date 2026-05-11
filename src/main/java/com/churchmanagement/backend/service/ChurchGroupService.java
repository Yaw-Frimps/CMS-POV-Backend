package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.ChurchGroupDto;
import com.churchmanagement.backend.model.ChurchGroup;
import com.churchmanagement.backend.repository.ChurchGroupRepository;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ChurchGroupService {

    private final ChurchGroupRepository repository;
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;

    public String uploadImage(MultipartFile file) {
        String id = java.util.UUID.randomUUID().toString();
        String storedFileName = fileStorageService.storeFile(file, "groups", id, null);
        return fileStorageService.getFileUrl("groups", id, storedFileName);
    }

    public List<ChurchGroupDto> getAllGroups() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @SuppressWarnings("null")
    public ChurchGroupDto createGroup(ChurchGroupDto dto) {
        ChurchGroup group = ChurchGroup.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .meetingSchedule(dto.getMeetingSchedule())
                .category(dto.getCategory() != null ? dto.getCategory() : "General")
                .build();
        ChurchGroup savedGroup = repository.save(group);
        return mapToDto(savedGroup);
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteGroup(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    @SuppressWarnings("null")
    public ChurchGroupDto updateGroup(Long id, ChurchGroupDto dto) {
        ChurchGroup group = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setImageUrl(dto.getImageUrl());
        group.setMeetingSchedule(dto.getMeetingSchedule());
        group.setCategory(dto.getCategory());
        ChurchGroup savedGroup = repository.save(group);
        return mapToDto(savedGroup);
    }

    @Transactional
    @SuppressWarnings("null")
    public ChurchGroupDto joinGroup(Long groupId, Long memberId) {
        ChurchGroup group = repository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (member.getGroups().size() >= 2) {
            throw new IllegalStateException("Members can only join a maximum of 2 groups");
        }

        if (!group.getMembers().contains(member)) {
            group.getMembers().add(member);
            member.getGroups().add(group);
            repository.save(group);
            memberRepository.save(member);
        }
        
        return mapToDto(group);
    }

    @Transactional
    @SuppressWarnings("null")
    public ChurchGroupDto leaveGroup(Long groupId, Long memberId) {
        ChurchGroup group = repository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (group.getMembers().contains(member)) {
            group.getMembers().remove(member);
            member.getGroups().remove(group);
            repository.save(group);
            memberRepository.save(member);
        }
        
        return mapToDto(group);
    }

    private ChurchGroupDto mapToDto(ChurchGroup group) {
        return ChurchGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .imageUrl(group.getImageUrl())
                .meetingSchedule(group.getMeetingSchedule())
                .memberCount(group.getMembers() != null ? group.getMembers().size() : 0)
                .category(group.getCategory() != null ? group.getCategory() : "General")
                .memberIds(group.getMembers() != null ? group.getMembers().stream().map(Member::getId).collect(Collectors.toList()) : java.util.Collections.emptyList())
                .build();
    }
}
