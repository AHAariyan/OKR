package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.OkrSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface OkrSheetRepository extends JpaRepository<OkrSheet, UUID> {
    List<OkrSheet> findByCycleId(UUID cycleId);

    List<OkrSheet> findByCycleIdAndScopeType(UUID cycleId, String scopeType);

    List<OkrSheet> findByCycleIdAndScopeTypeAndScopeId(UUID cycleId, String scopeType, UUID scopeId);

    List<OkrSheet> findByCycleIdAndScopeIdIn(UUID cycleId, List<UUID> scopeIds);
}
