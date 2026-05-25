package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class GroupResponse {
    private String groupId;
    private String name;
    private String description;
    private CreatorSummary creator;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
