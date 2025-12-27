package com.onnorokom.okr.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

@Data
public class SheetDetailDto {
    private UUID id;
    private String title; // Name of the scope owner
    private String cycleName;
    private String scopeType;
    private UUID scopeId;
    private LocalDate startDate;
    private LocalDate endDate;

    private Double computedOverallProgress;
    private Double computedTimeProgress;
    private Long daysRemaining;

    private List<ObjectiveDto> objectives;
}
