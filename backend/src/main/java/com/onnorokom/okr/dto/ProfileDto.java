package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProfileDto {
    private UUID id;
    private String email;
    private String name;
    private String teamName;
    private UUID teamId;
    private String departmentName;
    private UUID departmentId;
    private List<RoleInfo> roles;

    @Data
    public static class RoleInfo {
        private String role;
        private String scopeType;
        private UUID scopeId;
    }
}
