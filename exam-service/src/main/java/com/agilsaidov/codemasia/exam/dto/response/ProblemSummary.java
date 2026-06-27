package com.agilsaidov.codemasia.exam.dto.response;

import com.agilsaidov.codemasia.exam.model.Difficulty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProblemSummary {
    private Long problemId;
    private String title;
    private Difficulty difficulty;
}
