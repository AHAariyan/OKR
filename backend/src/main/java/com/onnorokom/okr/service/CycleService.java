package com.onnorokom.okr.service;

import com.onnorokom.okr.model.Cycle;
import java.util.Optional;

public interface CycleService {
    Optional<Cycle> getActiveCycle();

    double calculateTimeProgress(Cycle cycle);

    long calculateDaysRemaining(Cycle cycle);

    long calculateTotalDays(Cycle cycle);

    java.util.List<com.onnorokom.okr.dto.CycleDto> getAllCycles();

    com.onnorokom.okr.dto.CycleDto createCycle(com.onnorokom.okr.dto.CreateCycleRequest request);

    void deleteCycle(java.util.UUID cycleId);
}
