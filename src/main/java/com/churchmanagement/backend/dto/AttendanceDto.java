package com.churchmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long memberId;
    private String memberName;
    private LocalDateTime checkInTime;
    private String notes;
}
