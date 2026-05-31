package com.agilsaidov.codemasia.exam.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Table(name = "problems")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long problemId;

    @ManyToOne
    @JoinColumn(name = "exam_id",  nullable = false)
    private Exam exam;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "statement", nullable = false)
    private String statement;

    @Column(name = "time_limit_ms",  nullable = false)
    private Integer timeLimitMs = 1000;

    @Column(name = "memory_limit_kb",  nullable = false)
    private Integer memoryLimitKb = 128000;

    @Column(name = "points",   nullable = false)
    private Integer point = 100;

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
