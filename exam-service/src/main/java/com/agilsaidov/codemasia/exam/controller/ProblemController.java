package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.response.ProblemResponse;
import com.agilsaidov.codemasia.exam.dto.response.ProblemSummary;
import com.agilsaidov.codemasia.exam.model.Difficulty;
import com.agilsaidov.codemasia.exam.service.ProblemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
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


    @GetMapping
    public ResponseEntity<Page<ProblemSummary>> getProblems(@PathVariable String examId,
                                                            @RequestHeader("X-User-Id") UUID creatorId,
                                                            @RequestHeader("X-User-Role") String role,
                                                            @RequestParam(required = false) String title,
                                                            @RequestParam(required = false) Difficulty difficulty,
                                                            @RequestParam(required = false) OffsetDateTime createdAt,
                                                            @RequestParam(required = false) Integer point,
                                                            @RequestParam(required = false) Boolean enabled,
                                                            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page parameter cannot be negative") int page,
                                                            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size parameter must be at least 1") int size) {

        return ResponseEntity.ok(problemService.getProblems(examId, creatorId, role,
                title, difficulty, createdAt, point, enabled, page, size));
    }


    @GetMapping("/{problemId}")
    public ResponseEntity<ProblemResponse> getProblemDetails(@RequestHeader("X-User-Id") UUID creatorId,
                                                             @RequestHeader("X-User-Role") String role,
                                                             @PathVariable String examId,
                                                             @PathVariable Long problemId) {

        return ResponseEntity.ok(problemService.getProblemDetails(creatorId, role, examId, problemId));
    }


    @PutMapping("/{problemId}")
    public ResponseEntity<ProblemResponse> updateProblem(@RequestHeader("X-User-Id") UUID creatorId,
                                                         @RequestHeader("X-User-Role") String role,
                                                         @PathVariable String examId,
                                                         @PathVariable Long problemId,
                                                         @Valid @RequestBody UpdateProblemRequest request) {

        return ResponseEntity.ok(problemService.updateProblem(creatorId, role, examId, problemId, request));
    }


    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(@RequestHeader("X-User-Id") UUID creatorId,
                                              @RequestHeader("X-User-Role") String role,
                                              @PathVariable String examId,
                                              @PathVariable Long problemId) {
        problemService.deleteProblem(creatorId, role, examId, problemId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{problemId}/enable")
    public ResponseEntity<Void> enableProblem(@PathVariable String examId,
                                              @PathVariable Long problemId,
                                              @RequestParam boolean enabled) {

        problemService.enableProblem(examId, problemId, enabled);
        return ResponseEntity.ok().build();
    }

}
