package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.dto.response.UserResponse;
import com.agilsaidov.codemasia.user.mapper.UserMapper;
import com.agilsaidov.codemasia.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<UserResponse> getUsers(int  page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(userMapper::toUserResponse);
    }
}
