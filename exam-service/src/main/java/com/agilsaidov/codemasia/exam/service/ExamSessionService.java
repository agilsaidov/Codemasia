package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.client.GroupClient;
import com.agilsaidov.codemasia.exam.dto.clientdto.response.GroupDetails;
import com.agilsaidov.codemasia.exam.dto.request.CreateExamSessionRequest;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamSessionDetailsResponse;
import com.agilsaidov.codemasia.exam.exception.BadRequestException;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ProgrammingLanguageMapper;
import com.agilsaidov.codemasia.exam.model.*;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ExamSessionLanguageRepository;
import com.agilsaidov.codemasia.exam.repository.ExamSessionRepository;
import com.agilsaidov.codemasia.exam.repository.ProgrammingLanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final ExamSessionRepository examSessionRepository;
    private final ExamRepository examRepository;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final GroupClient groupClient;
    private final ExamSessionLanguageRepository examSessionLanguageRepository;
    private final ProgrammingLanguageMapper programmingLanguageMapper;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private static final List<SessionStatus> ACTIVE_SESSION_STATUSES =
            List.of(SessionStatus.SCHEDULED, SessionStatus.ACTIVE);

    @PostConstruct
    void init() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }


    public TeacherExamSessionDetailsResponse createExamSession(UUID creatorId, String role, CreateExamSessionRequest request) {
        log.debug("Creating exam session for exam={} group={} by creator={}", request.getExamId(), request.getGroupId(), creatorId);

        GroupDetails groupDetails = groupClient.getGroupById(request.getGroupId(), creatorId, role);

        Exam exam = "TEACHER".equals(role)
                ? getOwnedEnabledExam(creatorId, request.getExamId())
                : getExam(request.getExamId());

        if (!Boolean.TRUE.equals(exam.getPublishReady())) {
            throw new BadRequestException("EXAM_NOT_PUBLISH_READY",
                    "Exam must be publish-ready before creating a session");
        }

        if (!request.getStartsAt().isBefore(request.getEndsAt())) {
            throw new BadRequestException("INVALID_TIME_WINDOW", "Start time must be before end time");
        }

        if (examSessionRepository.existsOverlappingSession(
                request.getGroupId(),
                request.getStartsAt(),
                request.getEndsAt(),
                ACTIVE_SESSION_STATUSES)) {
            throw new BadRequestException("SESSION_TIME_CONFLICT",
                    "There is another active session for this group at that time interval");
        }

        List<ProgrammingLanguage> programmingLanguages = getProgrammingLanguages(request.getProgrammingLanguageIds());

        TeacherExamSessionDetailsResponse response = transactionTemplate.execute(status ->
                persistExamSession(creatorId, request, groupDetails, exam, programmingLanguages));

        log.info("Exam session={} created for exam={} group={} by creator={}",
                response.getExamSessionId(), request.getExamId(), request.getGroupId(), creatorId);
        return response;
    }


    private TeacherExamSessionDetailsResponse persistExamSession(UUID creatorId,
                                                                 CreateExamSessionRequest request,
                                                                 GroupDetails groupDetails,
                                                                 Exam exam,
                                                                 List<ProgrammingLanguage> programmingLanguages) {
        ExamSession examSession = ExamSession.builder()
                .exam(exam)
                .groupId(request.getGroupId())
                .assignedBy(creatorId)
                .examSessionTitle(request.getExamSessionTitle())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        ExamSession savedSession = examSessionRepository.save(examSession);

        List<ExamSessionLanguage> examSessionLanguages = programmingLanguages
                .stream()
                .map(pl -> new ExamSessionLanguage(
                        new ExamSessionLanguageId(
                                savedSession.getExamSessionId(),
                                pl.getJudge0LanguageId()),
                        savedSession,
                        pl
                ))
                .toList();

        examSessionLanguageRepository.saveAll(examSessionLanguages);

        return TeacherExamSessionDetailsResponse.builder()
                .examSessionId(savedSession.getExamSessionId())
                .examSessionTitle(savedSession.getExamSessionTitle())
                .examId(exam.getExamId())
                .examTitle(exam.getTitle())
                .groupId(groupDetails.getGroupId())
                .groupName(groupDetails.getName())
                .startsAt(savedSession.getStartsAt())
                .endsAt(savedSession.getEndsAt())
                .status(savedSession.getStatus())
                .publishReady(exam.getPublishReady())
                .selectionMode(savedSession.getSelectionMode())
                .useDifficultyTiers(savedSession.getUseDifficultyTiers())
                .questionQuota(savedSession.getQuestionQuota())
                .easyQuota(savedSession.getEasyQuota())
                .mediumQuota(savedSession.getMediumQuota())
                .hardQuota(savedSession.getHardQuota())
                .maxQuestionChanges(savedSession.getMaxQuestionChanges())
                .maxCheatEvents(savedSession.getMaxCheatEvents())
                .cheatBlockMode(savedSession.getCheatBlockMode())
                .createdAt(savedSession.getCreatedAt())
                .updatedAt(savedSession.getUpdatedAt())
                .programmingLanguages(
                        programmingLanguages.stream()
                                .map(programmingLanguageMapper::toProgrammingLanguageResponse)
                                .toList()
                )
                .build();
    }




    @Transactional
    public SessionCascadeResult cascadeOnExamDelete(Exam exam) {
        String examId = exam.getExamId();

        if (hasActiveSessions(examId)) {
            log.warn("Delete blocked for exam={}: one or more sessions are currently ACTIVE", examId);
            throw new BadRequestException(
                    "ACTIVE_SESSIONS_EXIST",
                    "Cannot delete exam [" + examId + "] while sessions are active"
            );
        }

        int cancelled = cancelScheduledSessions(examId);
        log.info("Exam={} cascade complete: {} SCHEDULED session(s) cancelled", examId, cancelled);
        return new SessionCascadeResult(cancelled);
    }

    @Transactional
    public void invalidateAssignmentReadiness(Exam exam) {
        String examId = exam.getExamId();

        ensureNoActiveSessions(examId);
        int cancelled = cancelScheduledSessions(examId);
        exam.setPublishReady(false);

        log.info("Exam={} assignment readiness invalidated: publishReady=false, {} scheduled session(s) cancelled",
                examId, cancelled);
    }

    @Transactional
    public int cancelScheduledSessions(String examId) {
        List<ExamSession> scheduled = examSessionRepository
                .findAllByExam_ExamIdAndStatus(examId, SessionStatus.SCHEDULED);

        for (ExamSession session : scheduled) {
            session.setStatus(SessionStatus.CANCELLED);
            session.setEnabled(false);
        }

        if (!scheduled.isEmpty()) {
            examSessionRepository.saveAll(scheduled);
        }

        return scheduled.size();
    }

    @Transactional(readOnly = true)
    public long countByExamId(String examId) {
        return examSessionRepository.countByExam_ExamId(examId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSessions(String examId) {
        return examSessionRepository.existsByExam_ExamIdAndStatus(examId, SessionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public void ensureNoActiveSessions(String examId) {
        if (hasActiveSessions(examId)) {
            log.warn("Operation blocked for exam={}: one or more sessions are currently ACTIVE", examId);
            throw new BadRequestException(
                    "ACTIVE_SESSIONS_EXIST",
                    "Cannot modify exam problems while sessions are active"
            );
        }
    }

    @Transactional(readOnly = true)
    public void ensureActiveSessionExists(String examId) {
        if (!hasActiveSessions(examId)) {
            log.warn("Hotfix blocked for exam={}: no ACTIVE session found", examId);
            throw new BadRequestException(
                    "NO_ACTIVE_SESSION",
                    "Test case hotfix is only allowed while exam sessions are active"
            );
        }
    }

    public record SessionCascadeResult(int cancelled) {}



    //Helper Methods
    private Exam getOwnedEnabledExam(UUID creatorId, String examId) {
        Exam exam = getExam(examId);

        if (!creatorId.equals(exam.getCreatorId())) {
            log.warn("Access denied: creator={} is not owner of exam={}", creatorId, examId);
            throw new ForbiddenException("FORBIDDEN_ACTION", "You are not allowed to access this resource");
        }

        if (!Boolean.TRUE.equals(exam.getEnabled())) {
            log.warn("Access denied: exam={} is disabled, requested by creator={}", examId, creatorId);
            throw new NotFoundException(
                    "EXAM_NOT_FOUND",
                    "Exam with id " + examId + " not found"
            );
        }

        return exam;
    }

    private Exam getExam(String examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> {
                    log.warn("Exam={} not found", examId);
                    return new NotFoundException(
                            "EXAM_NOT_FOUND",
                            "Exam with id " + examId + " not found");
                });
    }

    private List<ProgrammingLanguage> getProgrammingLanguages(List<Integer> languageIds) {
        return languageIds.stream()
                .distinct()
                .map(id -> programmingLanguageRepository
                        .findByJudge0LanguageIdAndEnabledTrue(id)
                        .orElseThrow(() -> new NotFoundException(
                                "LANGUAGE_NOT_FOUND",
                                "Language with id " + id + " not found"
                        )))
                .toList();
    }
}
