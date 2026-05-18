package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.dto.request.ChangePasswordRequest;
import com.agilsaidov.codemasia.user.dto.request.UpdateUserRequest;
import com.agilsaidov.codemasia.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.exception.BadRequestException;
import com.agilsaidov.codemasia.user.exception.ForbiddenException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
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
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    public Page<UserResponse> getUsers(Role role, int page, int size) {
        return userRepository.findAll(UserSpec.withFilters(role), PageRequest.of(page, size))
                .map(userMapper::toUserResponse);
    }

    public UserResponse getCurrentUser(String keycloakId) {
        UUID uuid = UUID.fromString(keycloakId);
        User user = userRepository.getUserByKeycloakId(uuid)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND" ,
                        "User not found")
                );
        return userMapper.toUserResponse(user);
    }


    public UserResponse updateUser(Long userId, String keycloakId, UpdateUserRequest request) {
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND" ,
                        "User with id: " + userId + " not found")
                );

        if(user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getKeycloakId())) {
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't update another admin");
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());

        return userMapper.toUserResponse(userRepository.save(user));
    }


    public void changePassword(Long userId, String keycloakId, ChangePasswordRequest request) {
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND" ,
                        "User with id: " + userId + " not found")
                );

        if(!request.getPassword().equals(request.getConfirmation())){
            throw new BadRequestException("PASSWORD_MISMATCH", "Password and confirmation do not match");
        }

        if(user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getKeycloakId())) {
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't change another admin's password");
        }

        keycloakAdminService.changePassword(user.getKeycloakId(), request.getPassword());
    }
}
