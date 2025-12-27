package com.onnorokom.okr.service;

import com.onnorokom.okr.model.User;
import java.util.UUID;

public interface AuditService {
    void logChange(User actor, String entityType, UUID entityId, String fieldName, String oldValue, String newValue);
}
