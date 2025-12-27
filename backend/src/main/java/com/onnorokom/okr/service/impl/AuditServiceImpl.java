package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.model.AuditLog;
import com.onnorokom.okr.model.User;
import com.onnorokom.okr.repository.AuditLogRepository;
import com.onnorokom.okr.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logChange(User actor, String entityType, UUID entityId, String fieldName, String oldValue,
            String newValue) {
        if ((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue))) {
            return;
        }

        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);

        auditLogRepository.save(log);
    }
}
