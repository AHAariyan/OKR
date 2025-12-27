package com.onnorokom.okr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "okr_sheets")
@Data
@NoArgsConstructor
public class OkrSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private Cycle cycle;

    @Column(name = "scope_type", nullable = false)
    private String scopeType; // COMPANY, TEAM, DEPARTMENT, PERSONAL

    @Column(name = "scope_id", nullable = false)
    private UUID scopeId;

    @Column(name = "is_held")
    private Boolean isHeld = false;

    @Column(name = "computed_overall_progress")
    private Double computedOverallProgress = 0.0;

    @Column(name = "computed_time_progress")
    private Double computedTimeProgress = 0.0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
