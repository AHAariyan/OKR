package com.onnorokom.okr.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateKeyResultRequest {
    private String title;
    private Double startValue = 0.0;
    private Double targetValue = 100.0;
    private LocalDate deadline;
}
