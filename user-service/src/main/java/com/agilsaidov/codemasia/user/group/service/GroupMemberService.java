package com.agilsaidov.codemasia.user.group.service;

import com.agilsaidov.codemasia.user.exception.BadRequestException;
import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
import com.agilsaidov.codemasia.user.group.dto.request.AddGroupMemberRequest;
import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.model.Group;
import com.agilsaidov.codemasia.user.group.model.GroupMember;
import com.agilsaidov.codemasia.user.group.model.GroupMemberId;
import com.agilsaidov.codemasia.user.group.repository.GroupMemberRepository;
import com.agilsaidov.codemasia.user.group.repository.GroupRepository;
import com.agilsaidov.codemasia.user.user.model.Role;
import com.agilsaidov.codemasia.user.user.model.User;
import com.agilsaidov.codemasia.user.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupService groupService;
    private final UserRepository userRepository;

    @Transactional
    public AdminGroupDetailsResponse addGroupMembers(String groupId, AddGroupMemberRequest request) {
        log.info("Adding members to group groupId={} userIds={}", groupId, request.getUserIds());

        Group group = groupRepository.findByIdWithCreator(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id:" + groupId + " not found");
                });

        if (!Boolean.TRUE.equals(group.getEnabled())) {
            log.warn("Group is disabled groupId={}", groupId);
            throw new BadRequestException("GROUP_DISABLED", "Group with id:" + groupId + " is disabled");
        }

        List<Long> userIds = request.getUserIds();
        if (userIds.size() != new HashSet<>(userIds).size()) {
            throw new BadRequestException("DUPLICATE_USER_IDS", "Request contains duplicate user ids");
        }

        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        Map<Long, GroupMember> existingMembersByUserId = groupMemberRepository
                .findByGroup_GroupIdAndId_UserIdIn(groupId, userIds).stream()
                .collect(Collectors.toMap(member -> member.getId().getUserId(), Function.identity()));

        List<GroupMember> membersToSave = new ArrayList<>();

        for (Long userId : userIds) {
            User user = usersById.get(userId);
            if (user == null) {
                log.warn("User not found userId={}", userId);
                throw new NotFoundException("USER_NOT_FOUND", "User with id: " + userId + " not found");
            }

            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new BadRequestException("USER_DISABLED", "User with id: " + userId + " is disabled");
            }

            if (user.getRole() != Role.STUDENT) {
                throw new BadRequestException("INVALID_MEMBER_ROLE",
                        "User with id: " + userId + " is not a student");
            }

            GroupMember existingMember = existingMembersByUserId.get(userId);

            if (existingMember != null) {
                if (Boolean.TRUE.equals(existingMember.getEnabled())) {
                    throw new DuplicateException("MEMBER_ALREADY_EXISTS",
                            "User with id: " + userId + " is already a member of group with id: " + groupId);
                }
                existingMember.setEnabled(true);
                membersToSave.add(existingMember);
                continue;
            }

            GroupMember member = new GroupMember();
            member.setId(new GroupMemberId(groupId, userId));
            member.setGroup(group);
            member.setUser(user);
            membersToSave.add(member);
        }

        groupMemberRepository.saveAll(membersToSave);

        AdminGroupDetailsResponse response = groupService.createAdminGroupResponse(group);
        log.info("Added members to group groupId={} count={} totalMembers={}",
                groupId, membersToSave.size(), response.getMembers().size());
        return response;
    }


    @Transactional
    public void enableGroupMember(String groupId, Long memberId, boolean enabled) {
        log.info("Member {} in group {} is setting to enabled={}", memberId, groupId, enabled);

        Group group = groupRepository.findByIdWithCreator(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id=" + groupId + " not found");
                });


        GroupMember member = groupMemberRepository.findById(new GroupMemberId(groupId, memberId))
                .orElseThrow(() -> {
                    log.warn("Member not found memberId={}", memberId);
                    return new NotFoundException("MEMBER_NOT_FOUND",
                            "User with id: " + memberId + " is not a member of group: " + groupId);
                });

        if(enabled == member.getEnabled()) {
            log.info("Member {} already has enabled={}, skipping", memberId, enabled);
            return;
        }

        member.setEnabled(enabled);
        groupMemberRepository.save(member);
        log.info("Member {} in group {} set to enabled={}", memberId, groupId, enabled);
    }
}
