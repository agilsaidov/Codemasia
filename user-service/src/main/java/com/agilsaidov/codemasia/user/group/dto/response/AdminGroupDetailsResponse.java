package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
public class AdminGroupDetailsResponse {
    private String groupId;
    private String name;
    private String description;
    private Boolean enabled;
    private CreatorSummary creator;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<MemberSummary> members;
    private List<AssignmentSummary> assignments;
}
