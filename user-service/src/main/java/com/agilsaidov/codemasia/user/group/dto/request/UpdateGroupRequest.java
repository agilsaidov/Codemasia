package com.agilsaidov.codemasia.user.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateGroupRequest {
    @NotBlank(message = "Field 'name' is required")
    @Size(max = 20, message = "Maximum allowed size for 'name' is 100")
    private String name;

    @Size(max = 500, message = "Maximum allowed size for 'description' is 500")
    private String description;
}
