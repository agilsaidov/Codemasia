package com.agilsaidov.codemasia.user.mapper;

import com.agilsaidov.codemasia.user.group.dto.response.GroupResponse;
import com.agilsaidov.codemasia.user.group.model.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(source = "createdBy.userId", target = "creator.id")
    @Mapping(source = "createdBy.name", target = "creator.name")
    @Mapping(source = "createdBy.surname", target = "creator.surname")
    GroupResponse toGroupResponse(Group group);
}
