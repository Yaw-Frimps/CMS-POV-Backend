package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.ExpenditureDto;
import com.churchmanagement.backend.model.Expenditure;
import com.churchmanagement.backend.repository.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;

    public List<ExpenditureDto> getAllExpenditures() {
        return expenditureRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ExpenditureDto createExpenditure(ExpenditureDto dto) {
        Expenditure expenditure = convertToEntity(dto);
        Expenditure saved = expenditureRepository.save(expenditure);
        return convertToDto(saved);
    }

    public void deleteExpenditure(Long id) {
        expenditureRepository.deleteById(id);
    }

    private ExpenditureDto convertToDto(Expenditure entity) {
        return ExpenditureDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .category(entity.getCategory())
                .date(entity.getDate())
                .build();
    }

    private Expenditure convertToEntity(ExpenditureDto dto) {
        return Expenditure.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .date(dto.getDate())
                .build();
    }
}
