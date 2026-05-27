package com.agilsaidov.codemasia.user.group.controller;

import com.agilsaidov.codemasia.user.group.dto.request.CreateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.response.AdminGroupDetailsResponse;
import com.agilsaidov.codemasia.user.group.dto.response.GroupSummary;
import com.agilsaidov.codemasia.user.group.dto.response.TeacherGroupDetailsResponse;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    //Create group
    @PostMapping("/groups")
    public ResponseEntity<AdminGroupDetailsResponse> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                                 @RequestHeader("X-User-Id") String keycloakId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request, keycloakId));
    }

    //Get groups
    @GetMapping("/groups")
    public ResponseEntity<Page<GroupSummary>> getGroups(@RequestParam(required = false) String name,
                                                        @RequestParam(required = false) Long creatorId,
                                                        @RequestParam(required = false) OffsetDateTime createdAt,
                                                        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page parameter cannot be negative") int page,
                                                        @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size parameter must be at least 1") int size) {
        return ResponseEntity.ok(groupService.getGroups(name, creatorId, createdAt, page, size));
    }

    //Admin
    @GetMapping("/groups/{id}")
    public ResponseEntity<?> getGroupById(
            @PathVariable("id") String groupId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String keycloakId) {

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(groupService.getGroupById(groupId));
        }
        return ResponseEntity.ok(groupService.getTeacherGroupById(keycloakId, groupId));
    }


    //Delete group
    //Update group
    //Add members
    //Remove members
    //Assign teacher
    //Unassign teacher
    //Activate assignment
}
