package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamRequest;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.service.ExamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@Validated
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<TeacherExamDetailsResponse> createExam(@Valid @RequestBody CreateExamRequest request,
                                                                 @RequestHeader("X-User-Id") UUID creatorId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.createExam(request, creatorId));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<TeacherExamDetailsResponse>> getTeacherExams(@RequestHeader("X-User-Id") UUID creatorId,
                                                                            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page parameter cannot be negative") int page,
                                                                            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size parameter must be at least 1") int size) {
        return ResponseEntity.ok(examService.getTeacherExams(creatorId, page, size));
    }

    @PatchMapping("/{examId}/publish")
    public ResponseEntity<Void> toggleExamPublishReady(@RequestHeader("X-User-Id") UUID creatorId,
                                                       @PathVariable String examId,
                                                       @RequestParam boolean publishReady) {
        examService.toggleExamPublishReady(creatorId, examId, publishReady);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{examId}/enable")
    public ResponseEntity<Void> enableExam(@RequestHeader("X-User-Id") UUID creatorId,
                                           @PathVariable String examId,
                                           @RequestParam boolean enabled) {
        examService.enableExam(creatorId, examId, enabled);
        return ResponseEntity.ok().build();
    }
}
