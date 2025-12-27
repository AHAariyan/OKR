package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CycleRepository extends JpaRepository<Cycle, UUID> {
    Optional<Cycle> findFirstByIsActiveTrue();
}
