package com.agilsaidov.codemasia.exam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class UpdateExamSessionRequest {

    @NotBlank(message = "Field 'groupId' is required")
    private String groupId;

    @NotBlank(message = "Field 'examSessionTitle' is required")
    private String examSessionTitle;

    @NotNull(message = "Field 'startsAt' is required")
    private OffsetDateTime startsAt;

    @NotNull(message = "Field 'endsAt' is required")
    private OffsetDateTime endsAt;
}
