package com.agilsaidov.codemasia.user.mapper;

import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.dto.response.AssignmentSummary;
import com.agilsaidov.codemasia.user.group.dto.response.GroupSummary;
import com.agilsaidov.codemasia.user.group.dto.response.MemberSummary;
import com.agilsaidov.codemasia.user.group.model.Group;
import com.agilsaidov.codemasia.user.group.model.GroupAssignment;
import com.agilsaidov.codemasia.user.group.model.GroupMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(source = "createdBy.userId", target = "creator.id")
    @Mapping(source = "createdBy.name", target = "creator.name")
    @Mapping(source = "createdBy.surname", target = "creator.surname")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    AdminGroupDetailsResponse toAdminGroupResponse(Group group);

    @Mapping(target = "memberCount", ignore = true)
    GroupSummary toGroupSummary(Group group);

    @Mapping(source = "teacher.userId", target = "teacher.id")
    @Mapping(source = "teacher.name", target = "teacher.name")
    @Mapping(source = "teacher.surname", target = "teacher.surname")
    AssignmentSummary toAssignmentSummary(GroupAssignment assignment);

    @Mapping(source = "user.userId", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.surname", target = "surname")
    @Mapping(source = "user.email", target = "email")
    MemberSummary toMemberSummary(GroupMember groupMember);
}
