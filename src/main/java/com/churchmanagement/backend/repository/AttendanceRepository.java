package com.churchmanagement.backend.repository;

import com.churchmanagement.backend.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEventId(Long eventId);

    List<Attendance> findByMemberId(Long memberId);
}
