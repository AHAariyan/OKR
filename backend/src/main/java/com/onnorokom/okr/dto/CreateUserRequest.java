package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateUserRequest {
    private String email;
    private String name; // Ideally we have name in User, but currently only email/pin/password. Using
                         // email as name for now.
    private String password;
    private String pin;
    private String role; // SUPER_ADMIN, MEMBER etc.
    private UUID teamId;
    private UUID departmentId;
}
