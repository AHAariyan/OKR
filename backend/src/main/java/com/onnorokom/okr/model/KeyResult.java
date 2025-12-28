package com.onnorokom.okr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "key_results")
@Data
@NoArgsConstructor
public class KeyResult {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objective_id", nullable = false)
    private Objective objective;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(name = "metric_type")
    private String metricType; // PERCENTAGE

    @Column(name = "start_value")
    private Double startValue = 0.0;

    @Column(name = "target_value")
    private Double targetValue = 100.0;

    @Column(name = "current_value")
    private Double currentValue = 0.0;

    @Column(name = "computed_progress")
    private Double computedProgress = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Column(name = "confidence_level")
    private String confidenceLevel; // ON_TRACK, etc

    private LocalDate deadline;

    @Column(name = "aligned_projects", columnDefinition = "TEXT")
    private String alignedProjects;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "weight")
    private Integer weight = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
