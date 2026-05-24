package com.agilsaidov.codemasia.user.group.service;

import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
import com.agilsaidov.codemasia.user.group.dto.request.CreateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.response.GroupResponse;
import com.agilsaidov.codemasia.user.group.model.Group;
import com.agilsaidov.codemasia.user.group.repository.GroupRepository;
import com.agilsaidov.codemasia.user.mapper.GroupMapper;
import com.agilsaidov.codemasia.user.user.model.User;
import com.agilsaidov.codemasia.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String keycloakId) {
        log.info("Creating group groupId={} name={} keycloakId={}", request.getGroupId(), request.getName(), keycloakId);

        User creator = userRepository.getUserByKeycloakId(UUID.fromString(keycloakId))
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Creator user not found"));

        String groupId = request.getGroupId();
        String name = request.getName();

        if(groupRepository.existsByGroupId(groupId)){
            log.warn("Group already exists with id={}", groupId);
            throw new DuplicateException("GROUP_ALREADY_EXISTS",
                    "Group with id:" + groupId + " already exists");
        }

        if(groupRepository.existsByName(name)){
            log.warn("Group already exists with name={}", name);
            throw new DuplicateException("GROUP_ALREADY_EXISTS",
                    "Group with name: " + name + " already exists");
        }

        Group group = Group.builder()
                .groupId(groupId)
                .name(name)
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        GroupResponse response = groupMapper.toGroupResponse(groupRepository.save(group));
        log.info("Group created groupId={} name={} createdBy={}", groupId, name, creator.getUserId());
        return response;
    }
}
