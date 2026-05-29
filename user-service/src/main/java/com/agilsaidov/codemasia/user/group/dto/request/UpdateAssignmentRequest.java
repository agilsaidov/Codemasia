package com.agilsaidov.codemasia.user.group.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class UpdateAssignmentRequest {
    @NotBlank(message = "Field 'title' is required")
    @Size(max = 100, message = "Maximum allowed size for 'title' is 100")
    private String title;

    @NotNull(message = "Field 'endsAt' is required")
    @FutureOrPresent(message = "endsAt must not be before the current time")
    private OffsetDateTime endsAt;
}
