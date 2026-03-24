package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.DonationDto;
import com.churchmanagement.backend.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    // Admin endpoint to view all church donations
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DonationDto>> getAllDonations() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }


    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<DonationDto>> getMemberDonations(@PathVariable Long memberId) {
        return ResponseEntity.ok(donationService.getDonationsByMemberId(memberId));
    }

    // Admin/Staff endpoint to manually log a donation
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DonationDto> createDonation(@RequestBody DonationDto donationDto) {
        return ResponseEntity.ok(donationService.createDonation(donationDto));
    }
}
