package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.AttendanceDto;
import com.churchmanagement.backend.model.Attendance;
import com.churchmanagement.backend.model.Event;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.repository.AttendanceRepository;
import com.churchmanagement.backend.repository.EventRepository;
import com.churchmanagement.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

        private final AttendanceRepository attendanceRepository;
        private final EventRepository eventRepository;
        private final MemberRepository memberRepository;

        public List<AttendanceDto> getAttendanceByEventId(Long eventId) {
                return attendanceRepository.findByEventId(eventId).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        public List<AttendanceDto> getAttendanceByMemberId(Long memberId) {
                return attendanceRepository.findByMemberId(memberId).stream()
                                .map(this::mapToDto)
                                .toList();
        }

        @Transactional
        @SuppressWarnings("null")
        public AttendanceDto checkIn(AttendanceDto attendanceDto) {
                Event event = eventRepository.findById(attendanceDto.getEventId())
                                .orElseThrow(() -> new RuntimeException("Event not found"));

                Member member = memberRepository.findById(attendanceDto.getMemberId())
                                .orElseThrow(() -> new RuntimeException("Member not found"));

                // Only allowing one check-in per user per event simply for this version
                Attendance attendance = Attendance.builder()
                                .event(event)
                                .member(member)
                                .notes(attendanceDto.getNotes())
                                .checkInTime(
                                                attendanceDto.getCheckInTime() != null ? attendanceDto.getCheckInTime()
                                                                : LocalDateTime.now())
                                .build();

                Attendance savedAttendance = attendanceRepository.save(attendance);
                return mapToDto(savedAttendance);
        }

        private AttendanceDto mapToDto(Attendance attendance) {
                return AttendanceDto.builder()
                                .id(attendance.getId())
                                .eventId(attendance.getEvent().getId())
                                .eventTitle(attendance.getEvent().getTitle())
                                .memberId(attendance.getMember().getId())
                                .memberName(attendance.getMember().getFirstName() + " "
                                                + attendance.getMember().getLastName())
                                .checkInTime(attendance.getCheckInTime())
                                .notes(attendance.getNotes())
                                .build();
        }
}
