package com.agilsaidov.codemasia.exam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "exam_sessions")
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long examSessionId;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "group_id", nullable = false, length = 20)
    private String groupId;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_mode", nullable = false, length = 10)
    private SelectionMode selectionMode = SelectionMode.RANDOM;

    @Column(name = "use_difficulty_tiers", nullable = false)
    private Boolean useDifficultyTiers = false;

    @Column(name = "question_quota", nullable = false)
    private Integer questionQuota = 0;

    @Column(name = "easy_quota", nullable = false)
    private Integer easyQuota = 0;

    @Column(name = "medium_quota", nullable = false)
    private Integer mediumQuota = 0;

    @Column(name = "hard_quota", nullable = false)
    private Integer hardQuota = 0;

    @Column(name = "max_question_changes", nullable = false)
    private Integer maxQuestionChanges = 0;

    @Column(name = "max_cheat_events", nullable = false)
    private Integer maxCheatEvents = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "cheat_block_mode", nullable = false, length = 20)
    private CheatBlockMode cheatBlockMode = CheatBlockMode.SUBMIT_BLOCKED;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (selectionMode == null) {
            selectionMode = SelectionMode.RANDOM;
        }
        if (useDifficultyTiers == null) {
            useDifficultyTiers = false;
        }
        if (questionQuota == null) {
            questionQuota = 0;
        }
        if (easyQuota == null) {
            easyQuota = 0;
        }
        if (mediumQuota == null) {
            mediumQuota = 0;
        }
        if (hardQuota == null) {
            hardQuota = 0;
        }
        if (maxQuestionChanges == null) {
            maxQuestionChanges = 0;
        }
        if (maxCheatEvents == null) {
            maxCheatEvents = 0;
        }
        if (cheatBlockMode == null) {
            cheatBlockMode = CheatBlockMode.SUBMIT_BLOCKED;
        }
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
