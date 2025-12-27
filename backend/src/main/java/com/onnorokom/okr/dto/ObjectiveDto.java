package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.UUID;
import java.util.List;

@Data
public class ObjectiveDto {
    private UUID id;
    private String title;
    private String ownerName;
    private Double computedProgress;
    private Integer sortOrder;

    private List<KeyResultDto> keyResults;
}
