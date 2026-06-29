package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamSessionRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateExamSessionRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateSessionRulesRequest;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamSessionDetailsResponse;
import com.agilsaidov.codemasia.exam.service.ExamSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exam-sessions")
@RequiredArgsConstructor
public class ExamSessionController {

    private final ExamSessionService examSessionService;

    @GetMapping("/{examSessionId}")
    public ResponseEntity<TeacherExamSessionDetailsResponse> getExamSessionDetails(@RequestHeader("X-User-Id") UUID userId,
                                                                                   @RequestHeader("X-User-Role") String role,
                                                                                   @PathVariable("examSessionId") Long sessionId) {
        return ResponseEntity.ok(examSessionService.getExamSessionDetails(userId, role, sessionId));
    }


    @PostMapping
    public ResponseEntity<TeacherExamSessionDetailsResponse> createExamSession(@RequestHeader("X-User-Id") UUID creatorId,
                                                                               @RequestHeader("X-User-Role") String role,
                                                                               @RequestBody @Valid CreateExamSessionRequest request){

        return ResponseEntity.ok(examSessionService.createExamSession(creatorId, role, request));
    }


    @PutMapping("/{examSessionId}")
    public ResponseEntity<TeacherExamSessionDetailsResponse> updateSession(@RequestHeader("X-User-Id") UUID creatorId,
                                                                           @RequestHeader("X-User-Role") String role,
                                                                           @PathVariable("examSessionId") Long sessionId,
                                                                           @RequestBody @Valid UpdateExamSessionRequest request){

        return ResponseEntity.ok(examSessionService.updateExamSession(creatorId, role, sessionId, request));
    }


    @PutMapping("/{examSessionId}/rules")
    public ResponseEntity<TeacherExamSessionDetailsResponse> updateSessionRules(@RequestHeader("X-User-Id") UUID creatorId,
                                                                                @RequestHeader("X-User-Role") String role,
                                                                                @PathVariable("examSessionId") Long sessionId,
                                                                                @RequestBody @Valid UpdateSessionRulesRequest request){
        return ResponseEntity.ok(examSessionService.updateExamSessionRules(creatorId, role, sessionId, request));
    }


    @PostMapping("/{examSessionId}/languages/{languageId}")
    public ResponseEntity<Void> addSessionProgrammingLanguage(@RequestHeader("X-User-Id") UUID creatorId,
                                                              @RequestHeader("X-User-Role") String role,
                                                              @PathVariable("examSessionId") Long sessionId,
                                                              @PathVariable Integer languageId){

        examSessionService.addSessionProgrammingLanguage(creatorId, role, sessionId, languageId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{examSessionId}/languages/{languageId}")
    public ResponseEntity<Void> removeSessionProgrammingLanguage(@RequestHeader("X-User-Id") UUID creatorId,
                                                                 @RequestHeader("X-User-Role") String role,
                                                                 @PathVariable("examSessionId") Long sessionId,
                                                                 @PathVariable Integer languageId){

        examSessionService.removeSessionProgrammingLanguage(creatorId, role, sessionId, languageId);
        return ResponseEntity.noContent().build();
    }
}
