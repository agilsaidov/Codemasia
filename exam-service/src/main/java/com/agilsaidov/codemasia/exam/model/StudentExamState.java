package com.agilsaidov.codemasia.exam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Table(name = "student_exam_states")
public class StudentExamState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long studentExamStateId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "change_count", nullable = false)
    private Integer changeCount = 0;

    @Column(name = "cheat_count", nullable = false)
    private Integer cheatCount = 0;

    @Column(name = "has_submitted", nullable = false)
    private Boolean hasSubmitted = false;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

    @Column(name = "block_reason", length = 150)
    private String blockReason;

    @Column(name = "blocked_at")
    private OffsetDateTime blockedAt;

    @Column(name = "paper_version", nullable = false)
    private Integer paperVersion = 1;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (changeCount == null) {
            changeCount = 0;
        }
        if (cheatCount == null) {
            cheatCount = 0;
        }
        if (hasSubmitted == null) {
            hasSubmitted = false;
        }
        if (blocked == null) {
            blocked = false;
        }
        if (paperVersion == null) {
            paperVersion = 1;
        }
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
