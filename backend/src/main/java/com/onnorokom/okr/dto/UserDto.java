package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String teamName;
    private String departmentName;
    private boolean isBlocked;
}
