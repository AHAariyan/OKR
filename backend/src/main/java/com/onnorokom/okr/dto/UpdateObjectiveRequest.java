package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UpdateObjectiveRequest {
    private String title;
    private UUID ownerUserId;
}
