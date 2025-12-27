package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;
import java.util.List;

@Data
public class SheetSummaryDto {
    private UUID id;
    private String scopeType; // COMPANY, TEAM, DEPARTMENT, PERSONAL
    private UUID scopeId;
    private String title; // "Company" or Team Name or User Name

    private Double computedOverallProgress;
    private Double computedTimeProgress;
    private Long daysRemaining;
    private Long totalDays;

    // For Dashboard card display
    private List<ObjectiveSummaryDto> objectives;

    @Data
    public static class ObjectiveSummaryDto {
        private String title;
        private Double progress;
    }
}
