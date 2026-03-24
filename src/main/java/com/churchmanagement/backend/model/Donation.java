package com.churchmanagement.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    private String fund; // e.g., "Tithe", "Building Fund", "Missions"

    private String paymentMethod; // e.g., "Credit Card", "Bank Transfer", "Cash"

    private String transactionId; // Reference for external payment gateway

    private LocalDateTime donationDate;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member; // The person who gave

    @PrePersist
    protected void onCreate() {
        if (donationDate == null) {
            donationDate = LocalDateTime.now();
        }
    }
}
