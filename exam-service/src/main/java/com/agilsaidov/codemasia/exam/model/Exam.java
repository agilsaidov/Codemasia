package com.agilsaidov.codemasia.exam.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
@Table(name = "exams")
public class Exam {
    @Id
    @Column(name = "id", length = 20)
    private String examId;

    @Column(name = "title",  nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_by", nullable = false)
    private UUID creatorId;

    @Builder.Default
    @Column(name = "publish_ready",  nullable = false)
    private Boolean publishReady = false;

    @Builder.Default
    @Column(name = "enabled",  nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at",  nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at",  nullable = false)
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
