package com.agilsaidov.codemasia.exam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class ExamSessionLanguageId implements Serializable {

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "judge0_language_id")
    private Integer judge0LanguageId;
}
