package com.agilsaidov.codemasia.user.controller;

import com.agilsaidov.codemasia.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Page<UserResponse>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok().body(userService.getUsers(page, size));
    }

}
