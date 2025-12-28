package com.onnorokom.okr.service;

import com.onnorokom.okr.model.KeyResult;
import com.onnorokom.okr.model.OkrSheet;
import com.onnorokom.okr.model.User;

public interface PermissionService {
    boolean canEditSheet(User actor, OkrSheet sheet);

    boolean canEditKeyResult(User actor, KeyResult kr);

    boolean isSuperAdmin(User actor);

    boolean isOrgLeadership(User actor);
}
