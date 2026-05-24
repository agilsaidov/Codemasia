package com.agilsaidov.codemasia.user.mapper;

import com.agilsaidov.codemasia.user.group.dto.response.GroupResponse;
import com.agilsaidov.codemasia.user.group.model.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(source = "createdBy.email", target = "createdBy")
    GroupResponse toGroupResponse(Group group);
}
