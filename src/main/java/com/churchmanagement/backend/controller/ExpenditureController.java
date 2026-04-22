package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.ExpenditureDto;
import com.churchmanagement.backend.service.ExpenditureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenditures")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @GetMapping
    public ResponseEntity<List<ExpenditureDto>> getAllExpenditures() {
        return ResponseEntity.ok(expenditureService.getAllExpenditures());
    }

    @PostMapping
    public ResponseEntity<ExpenditureDto> createExpenditure(@RequestBody ExpenditureDto expenditureDto) {
        return ResponseEntity.ok(expenditureService.createExpenditure(expenditureDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenditure(@PathVariable Long id) {
        expenditureService.deleteExpenditure(id);
        return ResponseEntity.noContent().build();
    }
}
