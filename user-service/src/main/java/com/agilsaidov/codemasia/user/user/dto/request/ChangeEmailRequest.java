package com.agilsaidov.codemasia.user.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeEmailRequest {

    @NotBlank(message = "Field email is required")
    @Email(message = "Field email must be a valid email address")
    private String email;
}
