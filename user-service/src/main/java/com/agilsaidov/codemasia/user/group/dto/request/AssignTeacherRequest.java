package com.agilsaidov.codemasia.user.group.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter
public class AssignTeacherRequest {
    @NotNull(message = "Field 'userId' is required")
    private UUID userId;

    @NotBlank(message = "Field 'title' is required")
    @Size(max = 100, message = "Maximum allowed size for 'title' is 100")
    private String title;

    @NotNull(message = "Field 'endsAt' is required")
    @FutureOrPresent(message = "endsAt must not be before the current time")
    private OffsetDateTime endsAt;
}
