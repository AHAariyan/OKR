package com.onnorokom.okr.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateKeyResultRequest {
    private String title;
    private Double currentValue;
    private Double startValue;
    private Double targetValue;
    private String confidenceLevel;
    private LocalDate deadline;
    private String alignedProjects;
    private String comments;
}
