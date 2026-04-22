package com.churchmanagement.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MeetingAttendanceRequest {

    @NotBlank(message = "Meeting name is required")
    private String meetingName;

    @NotNull(message = "Meeting date is required")
    private LocalDate meetingDate;

    @NotNull(message = "Attendee count is required")
    @PositiveOrZero(message = "Attendee count must be positive or zero")
    private Integer attendeeCount;
}
