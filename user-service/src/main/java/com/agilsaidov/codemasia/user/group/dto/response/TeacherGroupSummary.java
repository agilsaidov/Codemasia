package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeacherGroupSummary implements MemberCountable {
    private String groupId;
    private String name;
    private Integer memberCount;
}
