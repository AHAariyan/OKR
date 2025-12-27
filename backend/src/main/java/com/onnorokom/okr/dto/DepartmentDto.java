package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DepartmentDto {
    private UUID id;
    private String name;
    private String teamName;
    private UUID teamId;
}
