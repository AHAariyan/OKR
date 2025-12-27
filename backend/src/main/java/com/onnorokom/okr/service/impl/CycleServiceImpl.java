package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.model.Cycle;
import com.onnorokom.okr.repository.CycleRepository;
import com.onnorokom.okr.service.CycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class CycleServiceImpl implements CycleService {

    @Autowired
    private CycleRepository cycleRepository;

    @Override
    public Optional<Cycle> getActiveCycle() {
        return cycleRepository.findFirstByIsActiveTrue();
    }

    @Override
    public double calculateTimeProgress(Cycle cycle) {
        long totalDays = calculateTotalDays(cycle);
        if (totalDays <= 0)
            return 1.0;

        LocalDate now = LocalDate.now();
        if (now.isBefore(cycle.getStartDate()))
            return 0.0;
        if (now.isAfter(cycle.getEndDate()))
            return 1.0;

        long daysElapsed = ChronoUnit.DAYS.between(cycle.getStartDate(), now) + 1;
        return (double) daysElapsed / totalDays;
    }

    @Override
    public long calculateDaysRemaining(Cycle cycle) {
        LocalDate now = LocalDate.now();
        if (now.isAfter(cycle.getEndDate()))
            return 0;
        return ChronoUnit.DAYS.between(now, cycle.getEndDate());
    }

    @Override
    public long calculateTotalDays(Cycle cycle) {
        return ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate()) + 1;
    }

    @Override
    public java.util.List<com.onnorokom.okr.dto.CycleDto> getAllCycles() {
        return cycleRepository.findAll().stream().map(c -> {
            com.onnorokom.okr.dto.CycleDto dto = new com.onnorokom.okr.dto.CycleDto();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setStartDate(c.getStartDate());
            dto.setEndDate(c.getEndDate());
            dto.setIsActive(c.getIsActive());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public com.onnorokom.okr.dto.CycleDto createCycle(com.onnorokom.okr.dto.CreateCycleRequest request) {
        Cycle cycle = new Cycle();
        cycle.setName(request.getName());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setIsActive(request.getIsActive() != null ? request.getIsActive() : false);

        cycle = cycleRepository.save(cycle);

        com.onnorokom.okr.dto.CycleDto dto = new com.onnorokom.okr.dto.CycleDto();
        dto.setId(cycle.getId());
        dto.setName(cycle.getName());
        dto.setStartDate(cycle.getStartDate());
        dto.setEndDate(cycle.getEndDate());
        dto.setIsActive(cycle.getIsActive());
        return dto;
    }
}
