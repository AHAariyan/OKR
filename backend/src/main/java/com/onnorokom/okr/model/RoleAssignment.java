package com.onnorokom.okr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "role_assignments")
@Data
@NoArgsConstructor
public class RoleAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role; // SUPER_ADMIN, ORG_LEADERSHIP, TEAM_ADMIN, DEPARTMENT_ADMIN, MEMBER

    @Column(name = "scope_type", nullable = false)
    private String scopeType; // COMPANY, TEAM, DEPARTMENT, PERSONAL

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
