package com.agilsaidov.codemasia.user.group.service;

import com.agilsaidov.codemasia.user.exception.BadRequestException;
import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.exception.NotFoundException;
import com.agilsaidov.codemasia.user.group.dto.request.AssignTeacherRequest;
import com.agilsaidov.codemasia.user.group.dto.request.UpdateAssignmentRequest;
import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.model.Group;
import com.agilsaidov.codemasia.user.group.model.GroupAssignment;
import com.agilsaidov.codemasia.user.group.repository.GroupAssignmentRepository;
import com.agilsaidov.codemasia.user.group.repository.GroupRepository;
import com.agilsaidov.codemasia.user.user.model.Role;
import com.agilsaidov.codemasia.user.user.model.User;
import com.agilsaidov.codemasia.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupAssignmentService {

    private final GroupAssignmentRepository  groupAssignmentRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;


    @Transactional
    public AdminGroupDetailsResponse assignTeacher(String groupId, AssignTeacherRequest request) {
        Group group = groupRepository.findByIdWithCreator(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id: " + groupId + " not found");
                });

        Long teacherId = request.getUserId();

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> {
                    log.warn("User not found userId={}", teacherId);
                    return new NotFoundException("USER_NOT_FOUND",
                        "User with id:" + teacherId + " not found");
                });

        if(!Boolean.TRUE.equals(teacher.getEnabled())){
            throw new BadRequestException("USER_DISABLED", "Can not assign disabled user");
        }

        if(!Role.TEACHER.equals(teacher.getRole())) {
            throw new BadRequestException("INVALID_MEMBER_ROLE",
                    "User with id: " + teacherId + " is not a teacher");
        }

        if (groupAssignmentRepository.existsByGroup_GroupIdAndTeacher_UserIdAndActiveTrue(groupId, teacherId)) {
            throw new DuplicateException("ACTIVE_ASSIGNMENT_ALREADY_EXISTS",
                    "Teacher with id: " + teacherId + " already has an active assignment in this group");
        }

        GroupAssignment groupAssignment = new GroupAssignment();
        groupAssignment.setGroup(group);
        groupAssignment.setTeacher(teacher);
        groupAssignment.setTitle(request.getTitle());
        groupAssignment.setEndsAt(request.getEndsAt());
        groupAssignmentRepository.save(groupAssignment);

        log.info("Teacher={} successfully assigned to group={}", teacherId, groupId);
        return groupService.createAdminGroupResponse(group);
    }


    @Transactional
    public AdminGroupDetailsResponse updateAssignment(String groupId, Long assignmentId,
                                                      UpdateAssignmentRequest request) {
        Group group = groupRepository.findByIdWithCreator(groupId)
                .orElseThrow(() -> {
                    log.warn("Group not found groupId={}", groupId);
                    return new NotFoundException("GROUP_NOT_FOUND",
                            "Group with id: " + groupId + " not found");
                });

        GroupAssignment assignment = getAssignmentInGroup(groupId, assignmentId);

        assignment.setTitle(request.getTitle());
        assignment.setEndsAt(request.getEndsAt());

        groupAssignmentRepository.save(assignment);
        log.info("Assignment {} in group {} updated", assignmentId, groupId);
        return groupService.createAdminGroupResponse(group);
    }


    @Transactional
    public void enableAssignment(String groupId, Long assignmentId, boolean enabled) {
        log.info("Assignment {} in group {} is setting to enabled={}", assignmentId, groupId, enabled);

        if (!groupRepository.existsByGroupId(groupId)) {
            log.warn("Group not found groupId={}", groupId);
            throw new NotFoundException("GROUP_NOT_FOUND",
                    "Group with id: " + groupId + " not found");
        }

        GroupAssignment assignment = getAssignmentInGroup(groupId, assignmentId);

        if (enabled == Boolean.TRUE.equals(assignment.getActive())) {
            log.info("Assignment {} already has enabled={}, skipping", assignmentId, enabled);
            return;
        }

        if (enabled && groupAssignmentRepository.existsByGroup_GroupIdAndTeacher_UserIdAndActiveTrueAndAssignmentIdNot(
                groupId, assignment.getTeacher().getUserId(), assignmentId)) {
            throw new DuplicateException("ACTIVE_ASSIGNMENT_ALREADY_EXISTS",
                    "Teacher already has another active assignment in this group");
        }

        assignment.setActive(enabled);
        groupAssignmentRepository.save(assignment);
        log.info("Assignment {} in group {} set to enabled={}", assignmentId, groupId, enabled);
    }



    private GroupAssignment getAssignmentInGroup(String groupId, Long assignmentId) {
        GroupAssignment assignment = groupAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    log.warn("Assignment not found assignmentId={}", assignmentId);
                    return new NotFoundException("ASSIGNMENT_NOT_FOUND",
                            "Assignment with id: " + assignmentId + " not found");
                });

        if (!groupId.equals(assignment.getGroup().getGroupId())) {
            log.warn("Assignment {} does not belong to group {}", assignmentId, groupId);
            throw new NotFoundException("ASSIGNMENT_NOT_FOUND",
                    "Assignment with id: " + assignmentId + " not found in group: " + groupId);
        }

        return assignment;
    }

}
