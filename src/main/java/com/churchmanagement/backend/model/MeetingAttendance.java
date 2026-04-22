package com.churchmanagement.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "meeting_attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String meetingName;

    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(nullable = false)
    private Integer attendeeCount;
}
