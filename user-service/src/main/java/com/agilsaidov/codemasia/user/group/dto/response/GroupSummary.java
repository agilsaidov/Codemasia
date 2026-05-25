package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class GroupSummary {
    private String groupId;
    private String name;
    private OffsetDateTime createdAt;
    private Integer memberCount;
}
