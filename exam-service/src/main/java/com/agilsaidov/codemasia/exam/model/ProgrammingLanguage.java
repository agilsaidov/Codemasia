package com.agilsaidov.codemasia.exam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Table(name = "programming_languages")
public class ProgrammingLanguage {

    @Id
    @Column(name = "judge0_language_id")
    private Integer judge0LanguageId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (enabled == null) {
            enabled = true;
        }
        createdAt = OffsetDateTime.now();
    }
}
