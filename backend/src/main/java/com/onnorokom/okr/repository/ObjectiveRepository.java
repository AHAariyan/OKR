package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ObjectiveRepository extends JpaRepository<Objective, UUID> {
    List<Objective> findBySheetIdOrderBySortOrderAsc(UUID sheetId);

    List<Objective> findBySheetId(UUID sheetId);

    List<Objective> findByOwnerId(UUID ownerId);

    void deleteBySheetId(UUID sheetId);
}
