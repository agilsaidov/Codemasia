package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateExamRequest {
    @NotBlank(message = "Field 'title' is required")
    @Size(message = "Max allowed length for 'title' is 200", max = 200)
    private String title;

    @Size(message = "Max allowed length for 'description' is 500", max = 500)
    private String description;
}
