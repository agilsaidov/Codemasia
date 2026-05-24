package com.agilsaidov.codemasia.user.group.controller;

import com.agilsaidov.codemasia.user.group.dto.request.CreateGroupRequest;
import com.agilsaidov.codemasia.user.group.dto.response.GroupResponse;
import com.agilsaidov.codemasia.user.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    //Create group
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                     @RequestHeader("X-User-Id") String keycloakId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(request, keycloakId));
    }
    //Get groups
    //Delete group
    //Update group
    //Add members
    //Remove members
    //Assign teacher
    //Unassign teacher
    //Activate assignment
}
