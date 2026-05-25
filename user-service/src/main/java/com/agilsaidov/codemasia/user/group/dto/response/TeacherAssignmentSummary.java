package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class TeacherAssignmentSummary {
    private Long assignmentId;
    private OffsetDateTime assignedAt;
    private OffsetDateTime endsAt;
    private Boolean active;
}