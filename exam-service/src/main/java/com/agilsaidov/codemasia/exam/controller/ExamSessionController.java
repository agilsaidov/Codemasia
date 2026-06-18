package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamSessionRequest;
import com.agilsaidov.codemasia.exam.service.ExamSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exam-sessions")
@RequiredArgsConstructor
public class ExamSessionController {

    private final ExamSessionService examSessionService;

    @PostMapping
    public ResponseEntity<?> createExamSession(@RequestHeader("X-User-Id") UUID creatorId,
                                               @RequestHeader("X-User-Role") String role,
                                               @RequestBody @Valid CreateExamSessionRequest request){

        return ResponseEntity.ok(examSessionService.createExamSession(creatorId, role, request));
    }
}
