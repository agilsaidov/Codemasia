package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateTestCaseRequest;
import com.agilsaidov.codemasia.exam.dto.response.TestCaseResponse;
import com.agilsaidov.codemasia.exam.service.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams/{examId}/problems/{problemId}/test-cases")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping
    public ResponseEntity<TestCaseResponse> createTestCase(@PathVariable String examId,
                                                           @PathVariable Long problemId,
                                                           @RequestHeader("X-User-Id") UUID creatorId,
                                                           @RequestHeader("X-User-Role") String role,
                                                           @Valid @RequestBody CreateTestCaseRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testCaseService.createTestCase(examId, role, creatorId, problemId, request));
    }
}
