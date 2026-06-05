package com.agilsaidov.codemasia.exam.dto.request;

import com.agilsaidov.codemasia.exam.model.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateProblemRequest {

    @NotBlank(message = "Field 'title' is required")
    @Size(message = "Max allowed length for 'title' is 200", max = 200)
    private String title;

    @NotBlank(message = "Field 'statement' is required")
    private String statement;

    private Integer timeLimitMs;

    private Integer memoryLimitKb;

    private Integer point;

    @NotNull(message = "Field 'difficulty' is required")
    private Difficulty difficulty;
}
