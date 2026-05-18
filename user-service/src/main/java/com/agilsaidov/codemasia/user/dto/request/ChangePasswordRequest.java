package com.agilsaidov.codemasia.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Field password is required")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{7,}$",
            message = "Password must be at least 7 characters and contain at least one letter and one number"
    )
    private String password;

    @NotBlank(message = "Field confirmation is required")
    private String confirmation;
}
