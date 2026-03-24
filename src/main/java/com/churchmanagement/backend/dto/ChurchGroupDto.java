package com.churchmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChurchGroupDto {
    private Long id;
    private String name;
    private String description;
    private String meetingSchedule;
    private String category;
    private Integer memberCount;
    private java.util.List<Long> memberIds;
}
