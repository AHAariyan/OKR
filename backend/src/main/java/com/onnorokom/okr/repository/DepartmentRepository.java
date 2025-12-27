package com.onnorokom.okr.repository;

import com.onnorokom.okr.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
}
