package com.agilsaidov.codemasia.user.dto.request;

import com.agilsaidov.codemasia.user.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeRoleRequest {

    @NotNull(message = "Field role is required")
    private Role role;
}
