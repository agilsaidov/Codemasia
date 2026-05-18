package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.mapper.UserMapper;
import com.agilsaidov.codemasia.user.model.Role;
import com.agilsaidov.codemasia.user.model.User;
import com.agilsaidov.codemasia.user.repository.UserRepository;
import com.agilsaidov.codemasia.user.specification.UserSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<UserResponse> getUsers(Role role, int page, int size) {
        return userRepository.findAll(UserSpec.withFilters(role), PageRequest.of(page, size))
                .map(userMapper::toUserResponse);
    }

    public UserResponse getCurrentUser(String keycloakId) {
        UUID uuid = UUID.fromString(keycloakId);
        return userMapper.toUserResponse(userRepository.getUserByKeycloakId(uuid));
    }

}
