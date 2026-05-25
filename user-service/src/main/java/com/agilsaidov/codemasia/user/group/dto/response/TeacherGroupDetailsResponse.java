package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TeacherGroupDetailsResponse {
    private String groupId;
    private String name;
    private String description;
    private int memberCount;
    private List<MemberSummary> members;
    private TeacherAssignmentSummary myAssignment;
}