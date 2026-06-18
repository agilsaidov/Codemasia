package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
public class CreateExamSessionRequest {

    @NotBlank(message = "Field 'examId' is required")
    private String examId;

    @NotBlank(message = "Field 'groupId' is required")
    private String groupId;

    @NotBlank(message = "Field 'examSessionTitle' is required")
    private String examSessionTitle;

    @NotNull(message = "Field 'startsAt' is required")
    private OffsetDateTime startsAt;

    @NotNull(message = "Field 'endsAt' is required")
    private OffsetDateTime endsAt;

    @NotEmpty(message = "At least one language id must be included")
    private List<Integer> programmingLanguageIds;
}
