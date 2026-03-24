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
public class DonationDto {
    private Long id;
    private BigDecimal amount;
    private String fund;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime donationDate;
    private Long memberId;
    private String memberName; // Read-only for display convenience
}
