package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateExamRequest {
    @NotBlank(message = "Field 'title' is required")
    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String description;
}
