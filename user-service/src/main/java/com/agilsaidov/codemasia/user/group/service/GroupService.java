package com.agilsaidov.codemasia.user.group.service;

import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
import com.agilsaidov.codemasia.user.group.dto.request.CreateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.dto.response.GroupSummary;
import com.agilsaidov.codemasia.user.group.model.Group;
import com.agilsaidov.codemasia.user.group.repository.GroupMemberRepository;
import com.agilsaidov.codemasia.user.group.repository.GroupRepository;
import com.agilsaidov.codemasia.user.mapper.GroupMapper;
import com.agilsaidov.codemasia.user.specification.GroupSpec;
import com.agilsaidov.codemasia.user.user.model.User;
import com.agilsaidov.codemasia.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMapper groupMapper;

    @Transactional
    public AdminGroupDetailsResponse createGroup(CreateGroupRequest request, String keycloakId) {
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

        AdminGroupDetailsResponse response = groupMapper.toAdminGroupResponse(groupRepository.save(group));
        response.setMembers(List.of());
        response.setAssignments(List.of());
        log.info("Group created groupId={} name={} createdBy={}", groupId, name, creator.getUserId());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<GroupSummary> getGroups(String name, Long creatorId, OffsetDateTime createdAt, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groups = groupRepository.findAll(GroupSpec.withFilters(name, creatorId, createdAt), pageable);
        Page<GroupSummary> summaries = groups.map(groupMapper::toGroupSummary);
        enrichMemberCounts(summaries.getContent());
        return summaries;
    }



    private void enrichMemberCounts(List<GroupSummary> summaries) {
        if (summaries.isEmpty()) {
            return;
        }

        List<String> groupIds = summaries.stream().map(GroupSummary::getGroupId).toList();
        Map<String, Integer> memberCounts = new HashMap<>();

        for (Object[] row : groupMemberRepository.countMembersByGroupIds(groupIds)) {
            memberCounts.put((String) row[0], ((Long) row[1]).intValue());
        }

        summaries.forEach(summary ->
                summary.setMemberCount(memberCounts.getOrDefault(summary.getGroupId(), 0)));
    }
}
