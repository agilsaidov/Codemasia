package com.agilsaidov.codemasia.exam.dto.request;

import com.agilsaidov.codemasia.exam.model.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProblemRequest {
    @NotBlank(message = "Field 'title' is required")
    @Size(message = "Max allowed length for 'title' is 200", max = 200)
    private String title;

    @NotBlank(message = "Field 'statement' is required")
    private String statement;

    @NotNull(message = "Field 'timeLimitMs' is required")
    @Min(value = 1, message = "Field 'timeLimitMs' must be at least 1")
    @Max(value = 300_000, message = "Field 'timeLimitMs' cannot exceed 300000")
    private Integer timeLimitMs;

    @NotNull(message = "Field 'memoryLimitKb' is required")
    @Min(value = 1, message = "Field 'memoryLimitKb' must be at least 1")
    @Max(value = 1_048_576, message = "Field 'memoryLimitKb' cannot exceed 1048576")
    private Integer memoryLimitKb;

    @NotNull(message = "Field 'point' is required")
    @Min(value = 0, message = "Field 'point' cannot be negative")
    @Max(value = 50, message = "Field 'point' cannot exceed 50")
    private Integer point;

    @NotNull(message = "Field 'difficulty' is required")
    private Difficulty difficulty;
}
