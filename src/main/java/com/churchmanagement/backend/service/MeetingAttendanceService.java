package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.MeetingAttendanceRequest;
import com.churchmanagement.backend.model.MeetingAttendance;

import java.util.List;

public interface MeetingAttendanceService {
    MeetingAttendance createAttendance(MeetingAttendanceRequest request);
    List<MeetingAttendance> getAllAttendances();
    MeetingAttendance updateAttendance(Long id, MeetingAttendanceRequest request);
    void deleteAttendance(Long id);
}
