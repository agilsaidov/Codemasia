package com.agilsaidov.codemasia.user.group.controller;

import com.agilsaidov.codemasia.user.group.dto.request.AddGroupMemberRequest;
import com.agilsaidov.codemasia.user.group.dto.request.CreateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.request.UpdateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.dto.response.GroupSummary;
import com.agilsaidov.codemasia.user.group.service.GroupMemberService;
import com.agilsaidov.codemasia.user.group.service.GroupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;


    @PostMapping
    public ResponseEntity<AdminGroupDetailsResponse> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                                 @RequestHeader("X-User-Id") String keycloakId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request, keycloakId));
    }


    @GetMapping
    public ResponseEntity<Page<GroupSummary>> getGroups(@RequestParam(required = false) String name,
                                                        @RequestParam(required = false) Long creatorId,
                                                        @RequestParam(required = false) OffsetDateTime createdAt,
                                                        @RequestParam(required = false) Boolean enabled,
                                                        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page parameter cannot be negative") int page,
                                                        @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size parameter must be at least 1") int size) {
        return ResponseEntity.ok(groupService.getGroups(name, creatorId, createdAt, enabled, page, size));
    }


    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupById(
            @PathVariable String groupId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String keycloakId) {

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(groupService.getAdminGroupById(groupId));
        }
        return ResponseEntity.ok(groupService.getTeacherGroupById(keycloakId, groupId));
    }


    @PutMapping("/{groupId}")
    public ResponseEntity<AdminGroupDetailsResponse> updateGroup(@PathVariable String groupId,
                                                                 @Valid @RequestBody UpdateGroupRequest request){
        return ResponseEntity.ok(groupService.updateGroup(groupId, request));
    }


    @PatchMapping("/{groupId}/enable")
    public ResponseEntity<Void> enableGroup(@PathVariable String groupId,
                                            @RequestParam boolean enabled) {
        groupService.enableGroup(groupId, enabled);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{groupId}/members")
    public ResponseEntity<AdminGroupDetailsResponse> addGroupMembers(@PathVariable String groupId,
                                                                     @Valid @RequestBody AddGroupMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupMemberService.addGroupMembers(groupId, request));
    }

    //Delete group
    //Add members
    //Remove members
    //Assign teacher
    //Unassign teacher
    //Activate assignment
}
