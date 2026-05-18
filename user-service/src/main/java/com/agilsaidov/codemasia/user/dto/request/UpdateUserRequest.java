package com.agilsaidov.codemasia.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequest {
    @NotBlank(message = "Field name is required")
    private String name;

    @NotBlank(message = "Field surname is required")
    private String surname;
}
