package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.KeyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface KeyResultRepository extends JpaRepository<KeyResult, UUID> {
    List<KeyResult> findByObjectiveIdOrderBySortOrderAsc(UUID objectiveId);

    List<KeyResult> findByObjectiveId(UUID objectiveId);

    List<KeyResult> findByOwnerId(UUID ownerId);

    void deleteByObjectiveId(UUID objectiveId);
}
