package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.AttendanceDto;
import com.churchmanagement.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<AttendanceDto>> getEventAttendance(@PathVariable Long eventId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEventId(eventId));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<AttendanceDto>> getMemberAttendance(@PathVariable Long memberId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByMemberId(memberId));
    }

    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDto> checkIn(@RequestBody AttendanceDto attendanceDto) {
        return ResponseEntity.ok(attendanceService.checkIn(attendanceDto));
    }
}
