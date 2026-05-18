package com.agilsaidov.codemasia.user.mapper;

import com.agilsaidov.codemasia.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    User toUser(UserResponse response);
}
