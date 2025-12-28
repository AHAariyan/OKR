package com.onnorokom.okr.service;

import com.onnorokom.okr.dto.*;
import com.onnorokom.okr.model.User;
import java.util.UUID;

public interface OkrService {
    DashboardDto getDashboard(User user);

    HierarchicalDashboardDto getHierarchicalDashboard(User user);

    SheetDetailDto getSheetDetails(UUID sheetId);

    KeyResultDto updateKeyResult(UUID krId, UpdateKeyResultRequest request, User actor);

    ObjectiveDto updateObjective(UUID objectiveId, UpdateObjectiveRequest request, User actor);

    ObjectiveDto createObjective(UUID sheetId, CreateObjectiveRequest request, User actor);

    KeyResultDto createKeyResult(UUID objectiveId, CreateKeyResultRequest request, User actor);

    void deleteObjective(UUID objectiveId, User actor);

    void deleteKeyResult(UUID krId, User actor);
}
