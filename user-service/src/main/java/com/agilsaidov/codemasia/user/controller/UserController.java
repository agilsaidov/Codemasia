package com.agilsaidov.codemasia.user.controller;

import com.agilsaidov.codemasia.user.dto.request.ChangePasswordRequest;
import com.agilsaidov.codemasia.user.dto.request.UpdateUserRequest;
import com.agilsaidov.codemasia.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.model.Role;
import com.agilsaidov.codemasia.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(@RequestParam(required = false) Role role,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok().body(userService.getUsers(role, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") String keycloakId) {
        return ResponseEntity.ok(userService.getCurrentUser(keycloakId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable(name = "id") Long userId,
                                                   @RequestHeader("X-User-Id") String keycloakId,
                                                   @Valid @RequestBody UpdateUserRequest request){
        return ResponseEntity.ok(userService.updateUser(userId, keycloakId, request));
    }


    @PatchMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable(name = "id") Long userId,
                                               @RequestHeader("X-User-Id") String keycloakId,
                                               @Valid @RequestBody ChangePasswordRequest request){
        userService.changePassword(userId, keycloakId, request);
        return ResponseEntity.ok().build();
    }

}
