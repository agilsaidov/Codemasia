package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class AssignmentSummary {
    private Long assignmentId;
    private String title;
    private TeacherSummary teacher;
    private OffsetDateTime assignedAt;
    private OffsetDateTime endsAt;
    private Boolean enabled;
}