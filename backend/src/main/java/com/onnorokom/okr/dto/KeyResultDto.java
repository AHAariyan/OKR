package com.onnorokom.okr.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class KeyResultDto {
    private UUID id;
    private String title;
    private Double startValue;
    private Double targetValue;
    private Double currentValue;
    private Double computedProgress;

    private String ownerName;
    private String confidenceLevel;
    private LocalDate deadline;
    private String alignedProjects;
    private String comments;

    private Integer sortOrder;
    private Integer weight;

    private boolean canEdit;
}
