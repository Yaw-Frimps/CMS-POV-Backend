package com.churchmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenditureDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDateTime date;
}
