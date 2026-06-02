package com.agilsaidov.codemasia.exam.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteExamResponse {
    private String examId;
    private boolean deleted;
    private int sessionsCancelled;
    private int sessionsClosing;
    private int sessionsUnchanged;
}
