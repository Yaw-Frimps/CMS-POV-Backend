package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.DonationDto;
import com.churchmanagement.backend.model.Donation;
import com.churchmanagement.backend.model.Member;
import com.churchmanagement.backend.repository.DonationRepository;
import com.churchmanagement.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;

    public List<DonationDto> getAllDonations() {
        return donationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<DonationDto> getDonationsByMemberId(Long memberId) {
        return donationRepository.findByMemberId(memberId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DonationDto createDonation(DonationDto donationDto) {
        Member member = null;
        if (donationDto.getMemberId() != null) {
            member = memberRepository.findById(donationDto.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));
        }

        Donation donation = Donation.builder()
                .amount(donationDto.getAmount())
                .fund(donationDto.getFund())
                .paymentMethod(donationDto.getPaymentMethod())
                .transactionId(donationDto.getTransactionId())
                .donationDate(
                        donationDto.getDonationDate() != null ? donationDto.getDonationDate() : LocalDateTime.now())
                .member(member)
                .build();

        return mapToDto(donationRepository.save(donation));
    }

    private DonationDto mapToDto(Donation donation) {
        return DonationDto.builder()
                .id(donation.getId())
                .amount(donation.getAmount())
                .fund(donation.getFund())
                .paymentMethod(donation.getPaymentMethod())
                .transactionId(donation.getTransactionId())
                .donationDate(donation.getDonationDate())
                .memberId(donation.getMember() != null ? donation.getMember().getId() : null)
                .memberName(donation.getMember() != null
                        ? donation.getMember().getFirstName() + " " + donation.getMember().getLastName()
                        : "Anonymous")
                .build();
    }
}
