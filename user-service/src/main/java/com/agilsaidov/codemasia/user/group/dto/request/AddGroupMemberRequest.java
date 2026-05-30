package com.agilsaidov.codemasia.user.group.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public class AddGroupMemberRequest {
    @NotNull(message = "Field 'userIds' is required")
    @NotEmpty(message = "Field 'userIds' must not be empty")
    private List<UUID> userIds;
}
