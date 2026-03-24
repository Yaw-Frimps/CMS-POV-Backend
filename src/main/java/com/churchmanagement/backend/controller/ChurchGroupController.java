package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.ChurchGroupDto;
import com.churchmanagement.backend.service.ChurchGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class ChurchGroupController {

    private final ChurchGroupService service;

    @GetMapping
    public ResponseEntity<List<ChurchGroupDto>> getAllGroups() {
        return ResponseEntity.ok(service.getAllGroups());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChurchGroupDto> createGroup(@RequestBody ChurchGroupDto dto) {
        return ResponseEntity.ok(service.createGroup(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChurchGroupDto> updateGroup(
            @PathVariable Long id,
            @RequestBody ChurchGroupDto dto) {
        return ResponseEntity.ok(service.updateGroup(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        service.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ChurchGroupDto> joinGroup(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(service.joinGroup(groupId, memberId));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ChurchGroupDto> leaveGroup(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(service.leaveGroup(groupId, memberId));
    }
}
