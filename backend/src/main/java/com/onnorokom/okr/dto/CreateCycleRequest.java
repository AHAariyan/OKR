package com.onnorokom.okr.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateCycleRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
