package com.agilsaidov.codemasia.user.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateGroupRequest {
    @NotBlank(message = "Field 'groupId' is required")
    private String groupId;

    @NotBlank(message = "Field 'name' is required")
    private String name;

    private String description;
}
