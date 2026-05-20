package com.agilsaidov.codemasia.user.dto.request;

import com.agilsaidov.codemasia.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateUserRequest {

    @NotBlank(message = "Field email is required")
    @Email(message = "Field email must be a valid email address")
    private String email;

    @NotBlank(message = "Field username is required")
    private String username;

    @NotNull(message = "Field role is required")
    private Role role;

    @NotBlank(message = "Field password is required")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{7,}$",
            message = "Password must be at least 7 characters and contain at least one letter and one number"
    )
    private String password;

    @NotBlank(message = "Field name is required")
    private String name;

    @NotBlank(message = "Field surname is required")
    private String surname;
}
