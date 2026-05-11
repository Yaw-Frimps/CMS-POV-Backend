package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.MeetingAttendanceRequest;
import com.churchmanagement.backend.model.MeetingAttendance;
import com.churchmanagement.backend.repository.MeetingAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingAttendanceServiceImpl implements MeetingAttendanceService {

    private final MeetingAttendanceRepository meetingAttendanceRepository;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public MeetingAttendance createAttendance(MeetingAttendanceRequest request) {
        MeetingAttendance attendance = MeetingAttendance.builder()
                .meetingName(request.getMeetingName())
                .meetingDate(request.getMeetingDate())
                .attendeeCount(request.getAttendeeCount())
                .build();
        MeetingAttendance saved = meetingAttendanceRepository.save(attendance);
        return saved;
    }

    @Override
    public List<MeetingAttendance> getAllAttendances() {
        return meetingAttendanceRepository.findAllByOrderByMeetingDateAsc();
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public MeetingAttendance updateAttendance(Long id, MeetingAttendanceRequest request) {
        MeetingAttendance attendance = meetingAttendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        
        attendance.setMeetingName(request.getMeetingName());
        attendance.setMeetingDate(request.getMeetingDate());
        attendance.setAttendeeCount(request.getAttendeeCount());
        
        MeetingAttendance saved = meetingAttendanceRepository.save(attendance);
        return saved;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void deleteAttendance(Long id) {
        meetingAttendanceRepository.deleteById(id);
    }
}
