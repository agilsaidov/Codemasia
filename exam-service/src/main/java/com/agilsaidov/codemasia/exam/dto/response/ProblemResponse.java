package com.agilsaidov.codemasia.exam.dto.response;

import com.agilsaidov.codemasia.exam.model.Difficulty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class ProblemResponse {
    private Long problemId;
    private String title;
    private String examId;
    private String statement;
    private Integer timeLimitMs;
    private Integer memoryLimitKb;
    private Difficulty difficulty;
    private Boolean enabled;
    private OffsetDateTime createdAt;
}
