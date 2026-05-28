package com.agilsaidov.codemasia.user.group.service;

import com.agilsaidov.codemasia.user.exception.BadRequestException;
import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
import com.agilsaidov.codemasia.user.group.dto.request.CreateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.request.UpdateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.response.*;
import com.agilsaidov.codemasia.user.group.model.Group;
import com.agilsaidov.codemasia.user.group.model.GroupAssignment;
import com.agilsaidov.codemasia.user.group.model.GroupMember;
import com.agilsaidov.codemasia.user.group.model.GroupMemberId;
import com.agilsaidov.codemasia.user.group.repository.GroupAssignmentRepository;
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
    private final GroupAssignmentRepository groupAssignmentRepository;
    private final GroupMapper groupMapper;

    @Transactional
    public AdminGroupDetailsResponse createGroup(CreateGroupRequest request, String keycloakId) {
        log.info("Creating group groupId={} name={} keycloakId={}", request.getGroupId(), request.getName(), keycloakId);

        User creator = userRepository.getUserByKeycloakId(UUID.fromString(keycloakId))
                .orElseThrow(() -> {
                    log.warn("Creator user not found keycloakId={}", keycloakId);
                    return new NotFoundException("USER_NOT_FOUND", "Creator user not found");
                });

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
    public Page<GroupSummary> getGroups(String name, Long creatorId, OffsetDateTime createdAt, Boolean enabled, int page, int size) {
        log.debug("Fetching groups name={} creatorId={} createdAt={} enabled={} page={} size={}",
                name, creatorId, createdAt, enabled, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groups = groupRepository.findAll(GroupSpec.withFilters(name, creatorId, createdAt, enabled), pageable);
        Page<GroupSummary> summaries = groups.map(groupMapper::toGroupSummary);
        enrichMemberCounts(summaries.getContent());
        log.debug("Fetched groups totalElements={} page={} size={}", summaries.getTotalElements(), page, size);
        return summaries;
    }


    @Transactional(readOnly = true)
    public AdminGroupDetailsResponse getAdminGroupById(String groupId) {
        log.debug("Fetching group groupId={}", groupId);
        Group group = groupRepository.findByIdWithCreator(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id:" + groupId + " not found");
                });

        AdminGroupDetailsResponse response = createAdminGroupResponse(group);

        log.debug("Fetched group groupId={} members={} assignments={}", groupId, response.getMembers().size(), response.getAssignments().size());
        return response;
    }


    @Transactional(readOnly = true)
    public TeacherGroupDetailsResponse getTeacherGroupById(String keycloakId, String groupId) {
        log.debug("Fetching teacher group keycloakId={} groupId={}", keycloakId, groupId);

        User teacher = userRepository.getUserByKeycloakId(UUID.fromString(keycloakId))
                .orElseThrow(() -> {
                    log.warn("Teacher user not found keycloakId={}", keycloakId);
                    return new NotFoundException("USER_NOT_FOUND", "User with id:" + keycloakId + " not found");
                });

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND", "Group with id:" + groupId + " not found");
                });

        if (!Boolean.TRUE.equals(group.getEnabled())) {
            log.warn("Group is disabled groupId={}", groupId);
            throw new BadRequestException("GROUP_DISABLED", "Group with id:" + groupId + " is disabled");
        }

        GroupAssignment assignment = groupAssignmentRepository
                .findByGroup_GroupIdAndTeacher_UserId(groupId, teacher.getUserId())
                .orElseThrow(() -> {
                    log.warn("Teacher assignment not found teacherId={} groupId={}", teacher.getUserId(), groupId);
                    return new BadRequestException("ASSIGNMENT_NOT_FOUND",
                            "Teacher with id:" + teacher.getUserId()
                                    + " not assigned to group with id:" + groupId);
                });

        TeacherGroupDetailsResponse response = groupMapper.toTeacherGroupResponse(group);

        List<MemberSummary> members = groupMemberRepository.findAllWithUserByGroupId(groupId)
                .stream()
                .filter(member -> Boolean.TRUE.equals(member.getEnabled()))
                .map(groupMapper::toMemberSummary)
                .toList();

        response.setMembers(members);
        response.setMemberCount(members.size());
        response.setMyAssignment(groupMapper.toTeacherAssignmentSummary(assignment));

        log.debug("Fetched teacher group groupId={} teacherId={} members={}", groupId, teacher.getUserId(), members.size());
        return response;
    }

    public AdminGroupDetailsResponse updateGroup(String groupId, UpdateGroupRequest request) {
        log.info("Updating group groupId={} name={}", groupId, request.getName());

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id:" + groupId + " not found");
                });

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        Group updatedGroup = groupRepository.save(group);

        AdminGroupDetailsResponse response = createAdminGroupResponse(updatedGroup);
        log.info("Group updated groupId={} name={} members={} assignments={}",
                groupId, updatedGroup.getName(), response.getMembers().size(), response.getAssignments().size());

        return response;
    }

    @Transactional
    public void enableGroup(String groupId, boolean enabled) {
        log.info("Setting enabled={} for groupId={}", enabled, groupId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id:" + groupId + " not found");
                });
        group.setEnabled(enabled);
        groupRepository.save(group);
        log.info("Group enabled status updated groupId={} enabled={}", groupId, enabled);
    }



    AdminGroupDetailsResponse createAdminGroupResponse(Group group) {
        AdminGroupDetailsResponse response = groupMapper.toAdminGroupResponse(group);

        List<GroupAssignment> groupAssignments = groupAssignmentRepository.findAllWithTeacherByGroupId(group.getGroupId());
        List<AssignmentSummary> assignmentSummaries = groupAssignments.stream()
                .map(groupMapper::toAssignmentSummary).toList();

        List<GroupMember> groupMembers = groupMemberRepository.findAllWithUserByGroupId(group.getGroupId());
        List<MemberSummary> memberSummaries = groupMembers.stream()
                .map(groupMapper::toMemberSummary).toList();

        response.setAssignments(assignmentSummaries);
        response.setMembers(memberSummaries);
        return response;
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
