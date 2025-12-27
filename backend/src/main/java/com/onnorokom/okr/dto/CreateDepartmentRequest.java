package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateDepartmentRequest {
    private String name;
    private UUID teamId;
}
