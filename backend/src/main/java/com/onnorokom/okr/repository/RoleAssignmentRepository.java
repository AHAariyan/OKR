package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, UUID> {
    List<RoleAssignment> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    void deleteByScopeTypeAndScopeId(String scopeType, UUID scopeId);
}
