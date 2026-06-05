package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.response.ProblemResponse;
import com.agilsaidov.codemasia.exam.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams/{examId}/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@PathVariable String examId,
                                                         @RequestHeader("X-User-Id") UUID creatorId,
                                                         @RequestHeader("X-User-Role") String role,
                                                         @Valid @RequestBody CreateProblemRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.createProblem(examId, role, creatorId, request));
    }

}
