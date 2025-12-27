package com.onnorokom.okr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "cycles")
@Data
@NoArgsConstructor
public class Cycle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
