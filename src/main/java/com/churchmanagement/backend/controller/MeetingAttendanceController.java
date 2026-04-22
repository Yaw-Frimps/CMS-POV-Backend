package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.MeetingAttendanceRequest;
import com.churchmanagement.backend.model.MeetingAttendance;
import com.churchmanagement.backend.service.MeetingAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Assuming standard vite port for local dev
public class MeetingAttendanceController {

    private final MeetingAttendanceService meetingAttendanceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MeetingAttendance> createAttendance(@Valid @RequestBody MeetingAttendanceRequest request) {
        return new ResponseEntity<>(meetingAttendanceService.createAttendance(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MeetingAttendance>> getAllAttendances() {
        return ResponseEntity.ok(meetingAttendanceService.getAllAttendances());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MeetingAttendance> updateAttendance(@PathVariable Long id, @Valid @RequestBody MeetingAttendanceRequest request) {
        return ResponseEntity.ok(meetingAttendanceService.updateAttendance(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        meetingAttendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}
