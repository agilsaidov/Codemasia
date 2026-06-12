package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateTestCaseRequest {

    private String stdin;

    @NotBlank(message = "Field 'expectedOutput' is required")
    private String expectedOutput;

    private Boolean sample;

    @Min(value = 0, message = "Field 'position' cannot be negative")
    private Integer position;
}
