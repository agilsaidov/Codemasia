package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateTestCaseRequest {
    @NotBlank(message = "Field 'stdin' is required")
    private String stdin;

    @NotBlank(message = "Field 'expectedOutput' is required")
    private String expectedOutput;

    @NotNull(message = "Field 'sample' is required")
    private Boolean sample;

    @Min(value = 0, message = "Field 'position' cannot be negative")
    private Integer position;
}
