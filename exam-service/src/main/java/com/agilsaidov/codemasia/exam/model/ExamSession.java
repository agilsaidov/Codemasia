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
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
@Table(name = "exam_sessions")
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long examSessionId;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "exam_title", nullable = false, length = 200)
    private String examSessionTitle;

    @Column(name = "group_id", nullable = false, length = 20)
    private String groupId;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @Column(name = "starts_at")
    private OffsetDateTime startsAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "selection_mode", nullable = false, length = 10)
    private SelectionMode selectionMode = SelectionMode.RANDOM;

    @Builder.Default
    @Column(name = "use_difficulty_tiers", nullable = false)
    private Boolean useDifficultyTiers = false;

    //------- Points & Quota -------
    @Builder.Default
    @Column(name = "total_exam_points", nullable = false)
    private Double totalExamPoint = 50.0;


    @Builder.Default
    @Column(name = "question_quota", nullable = false)
    private Integer questionQuota = 0;

    @Builder.Default
    @Column(name = "question_quota_points", nullable = false)
    private Double questionQuotaPoint = 0.0;



    @Builder.Default
    @Column(name = "easy_quota", nullable = false)
    private Integer easyQuota = 0;

    @Builder.Default
    @Column(name = "easy_quota_points", nullable = false)
    private Double easyQuotaPoint = 0.0;



    @Builder.Default
    @Column(name = "medium_quota", nullable = false)
    private Integer mediumQuota = 0;

    @Builder.Default
    @Column(name = "medium_quota_points", nullable = false)
    private Double mediumQuotaPoint = 0.0;



    @Builder.Default
    @Column(name = "hard_quota", nullable = false)
    private Integer hardQuota = 0;

    @Builder.Default
    @Column(name = "hard_quota_points", nullable = false)
    private Double hardQuotaPoint = 0.0;

    //-----------------------------------

    @Builder.Default
    @Column(name = "max_question_changes", nullable = false)
    private Integer maxQuestionChanges = 0;

    @Builder.Default
    @Column(name = "max_cheat_events", nullable = false)
    private Integer maxCheatEvents = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "cheat_block_mode", nullable = false, length = 20)
    private CheatBlockMode cheatBlockMode = CheatBlockMode.SUBMIT_BLOCKED;

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
