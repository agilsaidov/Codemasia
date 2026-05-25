package com.agilsaidov.codemasia.user.mapper;

import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.dto.response.GroupSummary;
import com.agilsaidov.codemasia.user.group.model.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(source = "createdBy.userId", target = "creator.id")
    @Mapping(source = "createdBy.name", target = "creator.name")
    @Mapping(source = "createdBy.surname", target = "creator.surname")
    AdminGroupDetailsResponse toAdminGroupResponse(Group group);

    @Mapping(target = "memberCount", ignore = true)
    GroupSummary toGroupSummary(Group group);
}
