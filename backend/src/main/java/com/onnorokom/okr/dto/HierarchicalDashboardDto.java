package com.onnorokom.okr.dto;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class HierarchicalDashboardDto {
    private List<SheetSummaryDto> companyOkrs = new ArrayList<>();
    private List<TeamNode> teams = new ArrayList<>();
    private SheetSummaryDto myPersonalOkr;

    @Data
    public static class TeamNode {
        private String id;
        private String name;
        private SheetSummaryDto teamOkr;
        private List<DepartmentNode> departments = new ArrayList<>();
    }

    @Data
    public static class DepartmentNode {
        private String id;
        private String name;
        private SheetSummaryDto departmentOkr;
        private List<MemberNode> members = new ArrayList<>();
    }

    @Data
    public static class MemberNode {
        private String id;
        private String name;
        private String email;
        private SheetSummaryDto personalOkr;
    }
}
