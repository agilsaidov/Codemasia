package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotfixTestCaseRequest {

    private String stdin;

    private String expectedOutput;

    @NotBlank(message = "Field 'reason' is required")
    private String reason;
}
