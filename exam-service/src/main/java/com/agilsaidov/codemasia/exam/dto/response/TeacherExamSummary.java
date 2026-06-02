package com.agilsaidov.codemasia.exam.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeacherExamSummary {
    private String examId;
    private String title;
    private Boolean publishReady;
}
