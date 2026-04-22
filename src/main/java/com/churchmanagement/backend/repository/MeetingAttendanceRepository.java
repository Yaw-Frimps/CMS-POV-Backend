package com.churchmanagement.backend.repository;

import com.churchmanagement.backend.model.MeetingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingAttendanceRepository extends JpaRepository<MeetingAttendance, Long> {
    List<MeetingAttendance> findAllByOrderByMeetingDateAsc();
}
