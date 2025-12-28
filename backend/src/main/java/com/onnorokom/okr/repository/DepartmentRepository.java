package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByTeamId(UUID teamId);
}
