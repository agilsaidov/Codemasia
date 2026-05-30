package com.agilsaidov.codemasia.user.user.controller;

import com.agilsaidov.codemasia.user.user.dto.request.ChangeEmailRequest;
import com.agilsaidov.codemasia.user.user.dto.request.ChangePasswordRequest;
import com.agilsaidov.codemasia.user.user.dto.request.ChangeRoleRequest;
import com.agilsaidov.codemasia.user.user.dto.request.CreateUserRequest;
import com.agilsaidov.codemasia.user.user.dto.request.UpdateUserRequest;
import com.agilsaidov.codemasia.user.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.user.model.Role;
import com.agilsaidov.codemasia.user.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(@RequestParam(required = false) Role role,
                                                       @RequestParam(required = false) Boolean enabled,
                                                       @RequestParam(required = false) String email,
                                                       @RequestParam(required = false) String username,
                                                       @RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String surname,
                                                       @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page parameter cannot be negative")int page,
                                                       @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size parameter must be at least 1") int size) {

        return ResponseEntity.ok(userService.getUsers(role, enabled, email, username, name, surname, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") UUID userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") String keycloakId) {
        return ResponseEntity.ok(userService.getCurrentUser(keycloakId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable(name = "id") UUID userId,
                                                   @RequestHeader("X-User-Id") String keycloakId,
                                                   @Valid @RequestBody UpdateUserRequest request){
        return ResponseEntity.ok(userService.updateUser(userId, keycloakId, request));
    }


    @PatchMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable(name = "id") UUID userId,
                                               @RequestHeader("X-User-Id") String keycloakId,
                                               @Valid @RequestBody ChangePasswordRequest request){
        userService.changePassword(userId, keycloakId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/change-email")
    public ResponseEntity<UserResponse> changeEmail(@PathVariable(name = "id") UUID userId,
                                                    @RequestHeader("X-User-Id") String keycloakId,
                                                    @Valid @RequestBody ChangeEmailRequest request) {
        return ResponseEntity.ok(userService.changeEmail(userId, keycloakId, request));
    }

    @PatchMapping("/{id}/change-role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable(name = "id") UUID userId,
                                                   @Valid @RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(userService.changeRole(userId, request));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable(name = "id") UUID userId,
                                           @RequestHeader("X-User-Id") String keycloakId,
                                           @RequestParam boolean enabled) {
        userService.enableUser(userId, keycloakId ,enabled);
        return ResponseEntity.ok().build();
    }

}
