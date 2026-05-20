package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.dto.request.ChangePasswordRequest;
import com.agilsaidov.codemasia.user.dto.request.CreateUserRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    public Page<UserResponse> getUsers(Role role, int page, int size) {
        log.debug("Fetching users with role={} page={} size={}", role, page, size);
        return userRepository.findAll(UserSpec.withFilters(role), PageRequest.of(page, size))
                .map(userMapper::toUserResponse);
    }

    public UserResponse getCurrentUser(String keycloakId) {
        log.debug("Fetching current user keycloakId={}", keycloakId);
        UUID uuid = UUID.fromString(keycloakId);
        User user = userRepository.getUserByKeycloakId(uuid)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(Long userId, String keycloakId, UpdateUserRequest request) {
        log.info("Updating user userId={}", userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if (user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getKeycloakId())) {
            log.warn("Forbidden: attempt to update another admin userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't update another admin");
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        log.info("User updated successfully userId={}", userId);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void changePassword(Long userId, String keycloakId, ChangePasswordRequest request) {
        log.info("Changing password for userId={}", userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if (!request.getPassword().equals(request.getConfirmation())) {
            throw new BadRequestException("PASSWORD_MISMATCH", "Password and confirmation do not match");
        }

        if (user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getKeycloakId())) {
            log.warn("Forbidden: attempt to change another admin's password userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't change another admin's password");
        }

        keycloakAdminService.changePassword(user.getKeycloakId(), request.getPassword());
        log.info("Password changed successfully for userId={}", userId);
    }


    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user username={} role={}", request.getUsername(), request.getRole());
        UUID keycloakId = keycloakAdminService.createUser(request);

        try {
            keycloakAdminService.assignRole(keycloakId, request.getRole());

            User user = User.builder()
                    .keycloakId(keycloakId)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .name(request.getName())
                    .surname(request.getSurname())
                    .role(request.getRole())
                    .enabled(true)
                    .build();

            User saved = userRepository.save(user);
            log.info("User created successfully userId={} keycloakId={}", saved.getUserId(), keycloakId);
            return userMapper.toUserResponse(saved);

        } catch (RuntimeException e) {
            log.error("Failed to complete user creation, rolling back Keycloak user keycloakId={}", keycloakId);
            try {
                keycloakAdminService.deleteUser(keycloakId);
                log.info("Keycloak rollback successful keycloakId={}", keycloakId);
            } catch (Exception rollbackEx) {
                log.error("Keycloak rollback failed keycloakId={}: {}", keycloakId, rollbackEx.getMessage());
            }
            throw e;
        }
    }

    @Transactional
    public void enableUser(Long userId, boolean enabled) {
        log.info("Setting enabled={} for userId={}", enabled, userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        boolean previousEnabled = user.getEnabled();

        try{
            keycloakAdminService.enableUser(user.getKeycloakId(), enabled);
            user.setEnabled(enabled);
            userRepository.save(user);
            log.info("User enable status updated userId={}", userId);

        }catch (RuntimeException e){
            log.error("Failed to complete enable process, rolling back Keycloak changes for user keycloakId={}", user.getKeycloakId());
            try {
                keycloakAdminService.enableUser(user.getKeycloakId(), previousEnabled);
                log.info("Keycloak rollback successful keycloakId={}", user.getKeycloakId());

            } catch (Exception rollbackEx) {
                log.error("Keycloak rollback failed keycloakId={}: {}", user.getKeycloakId(), rollbackEx.getMessage());
            }
            throw e;
        }

    }
}
