package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDto {
    private List<SheetSummaryDto> sheets;
}
