package com.onnorokom.okr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "objectives")
@Data
@NoArgsConstructor
public class Objective {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sheet_id", nullable = false)
    private OkrSheet sheet;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "computed_progress")
    private Double computedProgress = 0.0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "objective", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<KeyResult> keyResults;
}
