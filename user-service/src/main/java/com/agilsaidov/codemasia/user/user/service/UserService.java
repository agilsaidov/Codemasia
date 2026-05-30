package com.agilsaidov.codemasia.user.user.service;

import com.agilsaidov.codemasia.user.user.dto.request.ChangeEmailRequest;
import com.agilsaidov.codemasia.user.user.dto.request.ChangePasswordRequest;
import com.agilsaidov.codemasia.user.user.dto.request.ChangeRoleRequest;
import com.agilsaidov.codemasia.user.user.dto.request.CreateUserRequest;
import com.agilsaidov.codemasia.user.user.dto.request.UpdateUserRequest;
import com.agilsaidov.codemasia.user.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.exception.BadRequestException;
import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.exception.ForbiddenException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
import com.agilsaidov.codemasia.user.mapper.UserMapper;
import com.agilsaidov.codemasia.user.user.model.Role;
import com.agilsaidov.codemasia.user.user.model.User;
import com.agilsaidov.codemasia.user.user.repository.UserRepository;
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

    public Page<UserResponse> getUsers(Role role, Boolean enabled,
                                       String email, String username,
                                       String name, String surname,
                                       int page, int size) {
        log.debug("Fetching users with role={} page={} size={}", role, page, size);
        return userRepository.findAll(UserSpec.withFilters(role, enabled, email, username, name, surname), PageRequest.of(page, size))
                .map(userMapper::toUserResponse);
    }


    public UserResponse getCurrentUser(String keycloakId) {
        log.debug("Fetching current user keycloakId={}", keycloakId);
        UUID uuid = UUID.fromString(keycloakId);
        User user = userRepository.getUserByUserId(uuid)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));
        return userMapper.toUserResponse(user);
    }


    public UserResponse getUserById(UUID userId) {
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User with id: " + userId + " not found"));

        return userMapper.toUserResponse(user);
    }


    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user username={} role={}", request.getUsername(), request.getRole());

        if(userRepository.existsByEmailOrUsername(request.getEmail(), request.getUsername())) {

            log.warn("User already exists in DB: username={} or email={}", request.getUsername(),  request.getEmail());

            throw new DuplicateException("USER_ALREADY_EXISTS",
                    "User with this username or email already exists");
        }

        UUID keycloakId = keycloakAdminService.createUser(request);

        try {
            keycloakAdminService.assignRole(keycloakId, request.getRole());

            User user = User.builder()
                    .userId(keycloakId)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .name(request.getName())
                    .surname(request.getSurname())
                    .role(request.getRole())
                    .enabled(true)
                    .build();

            User saved = userRepository.saveAndFlush(user);

            log.info("User created successfully userId={}", saved.getUserId());
            return userMapper.toUserResponse(saved);

        } catch (RuntimeException e) {
            log.error("Failed to complete user creation, rolling back Keycloak user keycloakId={}", keycloakId);
            try {
                keycloakAdminService.deleteUser(keycloakId);
                log.debug("Keycloak user rollback completed keycloakId={}", keycloakId);
            } catch (Exception rollbackEx) {
                log.error("Keycloak rollback failed keycloakId={}: {}", keycloakId, rollbackEx.getMessage());
            }
            throw e;
        }
    }


    public UserResponse updateUser(UUID userId, String keycloakId, UpdateUserRequest request) {
        log.info("Updating user userId={}", userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if (user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getUserId())) {
            log.warn("Forbidden: attempt to update another admin userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't update another admin");
        }

        user.setName(request.getName());
        user.setSurname(request.getSurname());
        log.info("User updated successfully userId={}", userId);
        return userMapper.toUserResponse(userRepository.save(user));
    }


    public void changePassword(UUID userId, String keycloakId, ChangePasswordRequest request) {
        log.info("Changing password for userId={}", userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if (!request.getPassword().equals(request.getConfirmation())) {
            throw new BadRequestException("PASSWORD_MISMATCH", "Password and confirmation do not match");
        }

        if (user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getUserId())) {
            log.warn("Forbidden: attempt to change another admin's password userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't change another admin's password");
        }

        keycloakAdminService.changePassword(user.getUserId(), request.getPassword());
        log.info("Password changed userId={}", userId);
    }


    @Transactional
    public UserResponse changeEmail(UUID userId, String keycloakId, ChangeEmailRequest request) {
        log.info("Changing email for userId={}", userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if (user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getUserId())) {
            log.warn("Forbidden: attempt to change another admin's email userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't change another admin's email");
        }

        if (request.getEmail().equals(user.getEmail())) {
            return userMapper.toUserResponse(user);
        }

        if (userRepository.existsByEmailAndUserIdNot(request.getEmail(), userId)) {
            throw new DuplicateException("USER_ALREADY_EXISTS", "User with this email already exists");
        }

        String previousEmail = user.getEmail();

        try {
            keycloakAdminService.changeEmail(user.getUserId(), request.getEmail());
            user.setEmail(request.getEmail());
            userRepository.saveAndFlush(user);
        } catch (RuntimeException e) {
            log.error("Failed to complete email change for userId={}", userId);
            try {
                keycloakAdminService.changeEmail(user.getUserId(), previousEmail);
                log.debug("Email change rollback completed keycloakId={}", user.getUserId());
            } catch (Exception rollbackEx) {
                log.error("Email change rollback failed for user keycloakId={}: {}", user.getUserId(), rollbackEx.getMessage());
            }
            throw e;
        }

        log.info("Email changed userId={} keycloakId={}", userId, user.getUserId());
        return userMapper.toUserResponse(user);
    }


    @Transactional
    public UserResponse changeRole(UUID userId, ChangeRoleRequest request) {
        Role role = request.getRole();
        log.info("Changing role userId={} to={}", userId, role);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if (user.getRole().equals(Role.ADMIN) || role.equals(Role.ADMIN)) {
            log.warn("Forbidden: attempt to change admin role userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't assign or change admin role");
        }

        Role previousRole = user.getRole();

        if (previousRole.equals(role)) {
            return userMapper.toUserResponse(user);
        }

        try{
            keycloakAdminService.assignRole(user.getUserId(), role);
            user.setRole(role);
            userRepository.saveAndFlush(user);
        }catch (RuntimeException e){
            log.error("Failed to complete role change for userId={}", userId);
            try{
                keycloakAdminService.assignRole(user.getUserId(), previousRole);
                log.debug("Role change rollback completed keycloakId={}", user.getUserId());
            }catch (Exception rollbackEx) {
                log.error("Keycloak 'user role' rollback failed for user keycloakId={}", user.getUserId());
            }
            throw e;
        }

        log.info("Role changed userId={} from={} to={}", userId, previousRole, role);
        return userMapper.toUserResponse(user);
    }


    @Transactional
    public void enableUser(UUID userId, String keycloakId, boolean enabled) {
        log.info("Setting enabled={} for userId={}", enabled, userId);
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND", "User with id: " + userId + " not found"));

        if(user.getRole().equals(Role.ADMIN) && !UUID.fromString(keycloakId).equals(user.getUserId())) {
            log.warn("Forbidden: attempt to enable/disable another admin userId={}", userId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "Can't enable or disable another admin");
        }

        boolean previousEnabled = user.getEnabled();

        try{
            keycloakAdminService.enableUser(user.getUserId(), enabled);
            user.setEnabled(enabled);
            userRepository.saveAndFlush(user);
            log.info("User enabled status updated userId={} enabled={}", userId, enabled);

        }catch (RuntimeException e){
            log.error("Failed to complete enable process, rolling back Keycloak changes for user keycloakId={}", user.getUserId());
            try {
                keycloakAdminService.enableUser(user.getUserId(), previousEnabled);
                log.debug("Enable rollback completed keycloakId={}", user.getUserId());

            } catch (Exception rollbackEx) {
                log.error("Keycloak 'enable user' rollback failed for user keycloakId={}: {}", user.getUserId(), rollbackEx.getMessage());
            }
            throw e;
        }
    }
}
