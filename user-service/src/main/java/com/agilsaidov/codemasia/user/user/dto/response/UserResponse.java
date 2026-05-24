package com.agilsaidov.codemasia.user.user.dto.response;

import com.agilsaidov.codemasia.user.user.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String name;
    private String surname;
    private Role role;
    private Boolean enabled;
}
