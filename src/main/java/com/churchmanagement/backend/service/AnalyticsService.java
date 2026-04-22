package com.churchmanagement.backend.service;

import com.churchmanagement.backend.model.Donation;
import com.churchmanagement.backend.model.Expenditure;
import com.churchmanagement.backend.repository.DonationRepository;
import com.churchmanagement.backend.repository.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DonationRepository donationRepository;
    private final ExpenditureRepository expenditureRepository;

    public Map<String, Object> getFinancialSummary() {
        List<Donation> donations = donationRepository.findAll();
        List<Expenditure> expenditures = expenditureRepository.findAll();

        BigDecimal totalDonations = donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenditures = expenditures.stream()
                .map(Expenditure::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netRevenue = totalDonations.subtract(totalExpenditures);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDonations", totalDonations);
        summary.put("totalExpenditures", totalExpenditures);
        summary.put("netRevenue", netRevenue);

        // Year-based breakdown
        Map<Integer, Map<String, BigDecimal>> yearlyData = new HashMap<>();
        
        donations.forEach(d -> {
            int year = d.getDonationDate().getYear();
            yearlyData.computeIfAbsent(year, k -> new HashMap<>());
            Map<String, BigDecimal> yearMap = yearlyData.get(year);
            yearMap.put("income", yearMap.getOrDefault("income", BigDecimal.ZERO).add(d.getAmount()));
        });

        expenditures.forEach(e -> {
            int year = e.getDate().getYear();
            yearlyData.computeIfAbsent(year, k -> new HashMap<>());
            Map<String, BigDecimal> yearMap = yearlyData.get(year);
            yearMap.put("expense", yearMap.getOrDefault("expense", BigDecimal.ZERO).add(e.getAmount()));
        });

        summary.put("yearlyBreakdown", yearlyData);
        
        return summary;
    }
}
