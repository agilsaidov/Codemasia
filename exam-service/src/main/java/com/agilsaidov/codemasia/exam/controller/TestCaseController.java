package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateTestCaseRequest;
import com.agilsaidov.codemasia.exam.dto.request.HotfixTestCaseRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateTestCaseRequest;
import com.agilsaidov.codemasia.exam.dto.response.TestCaseResponse;
import com.agilsaidov.codemasia.exam.service.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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


    @GetMapping
    public ResponseEntity<List<TestCaseResponse>> getTestCases(@PathVariable String examId,
                                                               @PathVariable Long problemId,
                                                               @RequestHeader("X-User-Id") UUID creatorId,
                                                               @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(testCaseService.getTestCases(examId, role, creatorId, problemId));
    }


    @PutMapping("/{testCaseId}")
    public ResponseEntity<TestCaseResponse> updateTestCase(@PathVariable String examId,
                                                           @PathVariable Long problemId,
                                                           @PathVariable Long testCaseId,
                                                           @RequestHeader("X-User-Id") UUID creatorId,
                                                           @RequestHeader("X-User-Role") String role,
                                                           @RequestBody @Valid UpdateTestCaseRequest request){

        return ResponseEntity.ok(testCaseService.updateTestCase(examId, problemId, testCaseId, role, creatorId, request));
    }


    @PatchMapping("/{testCaseId}/hotfix")
    public ResponseEntity<TestCaseResponse> hotfixTestCase(@PathVariable String examId,
                                                           @PathVariable Long problemId,
                                                           @PathVariable Long testCaseId,
                                                           @RequestHeader("X-User-Id") UUID adminId,
                                                           @Valid @RequestBody HotfixTestCaseRequest request) {
        return ResponseEntity.ok(testCaseService.hotfixTestCase(examId, problemId, testCaseId, adminId, request));
    }


    @DeleteMapping("/{testCaseId}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable String examId,
                                               @PathVariable Long problemId,
                                               @PathVariable Long testCaseId,
                                               @RequestHeader("X-User-Id") UUID creatorId,
                                               @RequestHeader("X-User-Role") String role){

        testCaseService.deleteTestCase(examId, problemId, testCaseId, creatorId, role);
        return ResponseEntity.ok().build();
    }
}
