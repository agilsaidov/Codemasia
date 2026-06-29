package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.client.GroupClient;
import com.agilsaidov.codemasia.exam.dto.clientdto.response.GroupDetails;
import com.agilsaidov.codemasia.exam.dto.request.CreateExamSessionRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateExamSessionRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateSessionRulesRequest;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamSessionDetailsResponse;
import com.agilsaidov.codemasia.exam.exception.BadRequestException;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ProgrammingLanguageMapper;
import com.agilsaidov.codemasia.exam.model.*;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ExamSessionLanguageRepository;
import com.agilsaidov.codemasia.exam.repository.ExamSessionRepository;
import com.agilsaidov.codemasia.exam.repository.ProblemRepository;
import com.agilsaidov.codemasia.exam.repository.ProgrammingLanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final ExamSessionRepository examSessionRepository;
    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final GroupClient groupClient;
    private final ExamSessionLanguageRepository examSessionLanguageRepository;
    private final ProgrammingLanguageMapper programmingLanguageMapper;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private static final List<SessionStatus> ACTIVE_SESSION_STATUSES =
            List.of(SessionStatus.SCHEDULED, SessionStatus.ACTIVE);

    private static final int POINT_SCALE = 2;

    @PostConstruct
    void init() {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }


    @Transactional(readOnly = true)
    public TeacherExamSessionDetailsResponse getExamSessionDetails(UUID userId, String role, Long sessionId) {
        log.debug("Fetching exam session={} for user={} role={}", sessionId, userId, role);

        ExamSession examSession = "TEACHER".equals(role)
                ? getOwnedAssignedExamSession(userId, sessionId)
                : getExamSession(sessionId);

        Exam exam = examSession.getExam();
        GroupDetails groupDetails = groupClient.getGroupById(examSession.getGroupId(), userId, role);
        List<ExamSessionLanguage> examSessionLanguages =
                examSessionLanguageRepository.findAllBySessionIdWithLanguages(sessionId);

        log.debug("Fetched exam session={} for user={} role={}", sessionId, userId, role);
        return toTeacherExamSessionDetailsResponse(examSession, exam, examSessionLanguages, groupDetails);
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

        if(OffsetDateTime.now().isAfter(request.getStartsAt())){
            throw new BadRequestException("INVALID_TIME", "You can't create a session in the past");
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

        return toTeacherExamSessionDetailsResponse(savedSession, exam, examSessionLanguages, groupDetails);

    }




    public TeacherExamSessionDetailsResponse updateExamSessionRules(UUID creatorId, String role, Long sessionId, UpdateSessionRulesRequest request) {
        log.debug("Updating rules for exam session={} by creator={}", sessionId, creatorId);

        ExamSession examSession = "TEACHER".equals(role)
                ? getOwnedAssignedExamSession(creatorId, sessionId)
                : getExamSession(sessionId);

        ensureSessionUpdatable(examSession);

        Exam exam = examSession.getExam();

        if (!Boolean.TRUE.equals(exam.getPublishReady())) {
            throw new BadRequestException(
                    "EXAM_NOT_PUBLISH_READY",
                    "Exam must be publish-ready before updating session rules"
            );
        }

        GroupDetails groupDetails = groupClient.getGroupById(examSession.getGroupId(), creatorId, role);

        TeacherExamSessionDetailsResponse response = transactionTemplate.execute(status -> {
            validateSessionRules(exam, request);
            return persistSessionRules(groupDetails, examSession, exam, request);
        });

        log.info("Updated rules for exam session={} by creator={}", sessionId, creatorId);

        return response;
    }



    private TeacherExamSessionDetailsResponse persistSessionRules(GroupDetails groupDetails,
                                                                 ExamSession examSession,
                                                                 Exam exam,
                                                                 UpdateSessionRulesRequest request) {

        examSession.setSelectionMode(request.getSelectionMode());
        examSession.setUseDifficultyTiers(request.getUseDifficultyTiers());
        examSession.setTotalExamPoint(fromPoints(request.getTotalExamPoint()));

        persistSelectionQuotas(examSession, exam.getExamId(), request);
        persistPointSettings(examSession, request);

        examSession.setMaxQuestionChanges(request.getMaxQuestionChanges());
        examSession.setMaxCheatEvents(request.getMaxCheatEvents());
        examSession.setCheatBlockMode(request.getCheatBlockMode());

        ExamSession updatedSession = examSessionRepository.save(examSession);

        List<ExamSessionLanguage> examSessionLanguages =
                examSessionLanguageRepository.findAllBySessionIdWithLanguages(updatedSession.getExamSessionId());

        return toTeacherExamSessionDetailsResponse(updatedSession, exam, examSessionLanguages, groupDetails);
    }



    public TeacherExamSessionDetailsResponse updateExamSession(UUID creatorId,
                                                             String role,
                                                             Long sessionId,
                                                             UpdateExamSessionRequest request) {
        log.debug("Updating exam session={} by creator={}", sessionId, creatorId);

        ExamSession examSession = "TEACHER".equals(role)
                ? getOwnedAssignedExamSession(creatorId, sessionId)
                : getExamSession(sessionId);

        ensureSessionUpdatable(examSession);

        Exam exam = examSession.getExam();

        if (!Boolean.TRUE.equals(exam.getPublishReady())) {
            throw new BadRequestException("EXAM_NOT_PUBLISH_READY",
                    "Exam must be publish-ready before updating a session");
        }

        if (OffsetDateTime.now().isAfter(request.getStartsAt())) {
            throw new BadRequestException("INVALID_TIME", "You can't set a session start time in the past");
        }

        if (!request.getStartsAt().isBefore(request.getEndsAt())) {
            throw new BadRequestException("INVALID_TIME_WINDOW", "Start time must be before end time");
        }

        if (examSessionRepository.existsOverlappingSessionExcluding(
                request.getGroupId(),
                request.getStartsAt(),
                request.getEndsAt(),
                sessionId,
                ACTIVE_SESSION_STATUSES)) {
            throw new BadRequestException("SESSION_TIME_CONFLICT",
                    "There is another active session for this group at that time interval");
        }

        GroupDetails groupDetails = groupClient.getGroupById(request.getGroupId(), creatorId, role);

        TeacherExamSessionDetailsResponse response = transactionTemplate.execute(status ->
                persistUpdatedExamSession(examSession, request, groupDetails, exam));

        log.info("Exam session={} updated by creator={}", sessionId, creatorId);
        return response;
    }


    private TeacherExamSessionDetailsResponse persistUpdatedExamSession(ExamSession examSession,
                                                                        UpdateExamSessionRequest request,
                                                                        GroupDetails groupDetails,
                                                                        Exam exam) {

        examSession.setGroupId(request.getGroupId());
        examSession.setExamSessionTitle(request.getExamSessionTitle());
        examSession.setStartsAt(request.getStartsAt());
        examSession.setEndsAt(request.getEndsAt());

        ExamSession updatedSession = examSessionRepository.save(examSession);
        List<ExamSessionLanguage> examSessionLanguages = examSessionLanguageRepository.findAllBySessionIdWithLanguages(updatedSession.getExamSessionId());

        return toTeacherExamSessionDetailsResponse(updatedSession, exam, examSessionLanguages, groupDetails);
    }


    @Transactional
    public void addSessionProgrammingLanguage(UUID creatorId, String role,
                                       Long sessionId, Integer languageId) {

        log.debug("Adding language={} to exam session={}", languageId, sessionId);

        ExamSession examSession = "TEACHER".equals(role)
                ? getOwnedAssignedExamSession(creatorId, sessionId)
                : getExamSession(sessionId);

        ensureSessionUpdatable(examSession);


        if (!Boolean.TRUE.equals(examSession.getExam().getPublishReady())) {
            throw new BadRequestException("EXAM_NOT_PUBLISH_READY",
                    "Exam must be publish-ready before updating a session");
        }

        if (examSessionLanguageRepository.existsById(new ExamSessionLanguageId(sessionId, languageId))) {
            throw new BadRequestException(
                    "LANGUAGE_ALREADY_EXISTS",
                    "Session already has this language"
            );
        }

        ProgrammingLanguage newProgrammingLanguage = programmingLanguageRepository
                .findByJudge0LanguageIdAndEnabledTrue(languageId)
                .orElseThrow(() -> new NotFoundException(
                        "LANGUAGE_NOT_FOUND",
                        "Language with id: " + languageId + " not found")
                );


        ExamSessionLanguage newExamSessionLanguage = new ExamSessionLanguage(
                new ExamSessionLanguageId(sessionId, languageId),
                examSession,
                newProgrammingLanguage
        );

        examSessionLanguageRepository.save(newExamSessionLanguage);

        log.info("Added language={} to exam session={}", languageId, sessionId);
    }


    @Transactional
    public void removeSessionProgrammingLanguage(UUID creatorId, String role, Long sessionId, Integer languageId) {

        log.debug("Removing language={} from exam session={}", languageId, sessionId);

        ExamSession examSession = "TEACHER".equals(role)
                ? getOwnedAssignedExamSession(creatorId, sessionId)
                : getExamSession(sessionId);

        ensureSessionUpdatable(examSession);


        if (!Boolean.TRUE.equals(examSession.getExam().getPublishReady())) {
            throw new BadRequestException("EXAM_NOT_PUBLISH_READY",
                    "Exam must be publish-ready before updating a session");
        }

        ExamSessionLanguage examSessionLanguage = examSessionLanguageRepository.findById(new ExamSessionLanguageId(sessionId, languageId))
                .orElseThrow(() -> new NotFoundException(
                        "EXAM_SESSION_LANGUAGE_NOT_FOUND",
                        "Language with id: " + languageId + " not found in this exam session"
                ));

        if (examSessionLanguageRepository.countBySession_ExamSessionId(sessionId) <= 1) {
            throw new BadRequestException("LAST_LANGUAGE",
                    "Session must have at least one programming language");
        }

        examSessionLanguageRepository.delete(examSessionLanguage);
        log.info("Removed language={} from exam session={}", languageId, sessionId);

    }

    //Service internal methods
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

    private ExamSession getExamSession(Long sessionId) {
        return examSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("Exam session={} not found", sessionId);
                    return new NotFoundException(
                            "EXAM_SESSION_NOT_FOUND",
                            "Session with id: " + sessionId + " not found");
                });
    }

    private ExamSession getOwnedAssignedExamSession(UUID creatorId, Long sessionId) {
        ExamSession examSession = getExamSession(sessionId);

        if (!creatorId.equals(examSession.getAssignedBy())) {
            log.warn("Access denied: creator={} is not assigner of session={}", creatorId, sessionId);
            throw new ForbiddenException("ACCESS_DENIED", "You do not have permission to access this resource");
        }

        getOwnedEnabledExam(creatorId, examSession.getExam().getExamId());
        return examSession;
    }

    private void ensureSessionUpdatable(ExamSession examSession) {
        SessionStatus status = examSession.getStatus();
        OffsetDateTime now = OffsetDateTime.now();

        if (status == SessionStatus.FINISHED || status == SessionStatus.CANCELLED) {
            throw new ForbiddenException(
                    "UPDATE_NOT_ALLOWED",
                    "You can't update a finished or cancelled exam session"
            );
        }

        OffsetDateTime startsAt = examSession.getStartsAt();
        if (startsAt != null && !now.isBefore(startsAt)) {
            throw new ForbiddenException(
                    "UPDATE_NOT_ALLOWED",
                    "You can't update an exam session once it has started"
            );
        }
    }


    private void validateSessionRules(Exam exam, UpdateSessionRulesRequest request) {
        String examId = exam.getExamId();
        BigDecimal totalExamPoint = toPoints(request.getTotalExamPoint());
        validatePositiveTotalExamPoint(totalExamPoint);

        if (request.getSelectionMode() == SelectionMode.FIXED) {
            validateSelectionQuotasAreZero(request);
            if (Boolean.TRUE.equals(request.getUseDifficultyTiers())) {
                validateTierPointsOnly(request);
                validateEnabledProblemsHaveDifficulty(examId);
                validateFixedTierPoints(examId, request, totalExamPoint);
            } else {
                validateFlatPointsOnly(request);
                validateFixedFlatPoints(examId, request, totalExamPoint);
            }
            return;
        }

        if (Boolean.TRUE.equals(request.getUseDifficultyTiers())) {
            validateEnabledProblemsHaveDifficulty(examId);
            validateRandomTierQuotas(examId, request);
            validateTierPointsOnly(request);
            validateRandomTierPoints(request, totalExamPoint);
            return;
        }

        validateFlatPointsOnly(request);
        validateRandomFlatQuotas(examId, request);
        validateRandomFlatPoints(request, totalExamPoint);
    }

    private void validatePositiveTotalExamPoint(BigDecimal totalExamPoint) {
        if (totalExamPoint.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "INVALID_TOTAL_POINT",
                    "Total exam point must be greater than zero"
            );
        }
    }

    private void validateSelectionQuotasAreZero(UpdateSessionRulesRequest request) {
        if (request.getQuestionQuota() > 0
                || request.getEasyQuota() > 0
                || request.getMediumQuota() > 0
                || request.getHardQuota() > 0) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    "Selection quotas must be zero when selection mode is FIXED"
            );
        }
    }

    private void validateTierPointsOnly(UpdateSessionRulesRequest request) {
        requireZeroPoint(request.getQuestionQuotaPoint(), "questionQuotaPoint");
        requireZeroTierQuotasWhenTierPointsMode(request);
    }

    private void validateFlatPointsOnly(UpdateSessionRulesRequest request) {
        requireZeroPoint(request.getEasyQuotaPoint(), "easyQuotaPoint");
        requireZeroPoint(request.getMediumQuotaPoint(), "mediumQuotaPoint");
        requireZeroPoint(request.getHardQuotaPoint(), "hardQuotaPoint");
        requireZeroTierQuotasWhenFlatMode(request);
    }

    private void requireZeroTierQuotasWhenTierPointsMode(UpdateSessionRulesRequest request) {
        if (request.getQuestionQuota() > 0) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    "Question quota must be zero when difficulty tiers are enabled"
            );
        }
    }

    private void requireZeroTierQuotasWhenFlatMode(UpdateSessionRulesRequest request) {
        if (request.getEasyQuota() > 0 || request.getMediumQuota() > 0 || request.getHardQuota() > 0) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    "Difficulty tier quotas must be zero when difficulty tiers are disabled"
            );
        }
    }

    private void validateRandomTierQuotas(String examId, UpdateSessionRulesRequest request) {
        int easyQuota = request.getEasyQuota();
        int mediumQuota = request.getMediumQuota();
        int hardQuota = request.getHardQuota();
        int tierTotal = easyQuota + mediumQuota + hardQuota;

        if (tierTotal <= 0) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    "At least one difficulty tier quota must be greater than zero"
            );
        }

        validateTierQuota(examId, Difficulty.EASY, easyQuota);
        validateTierQuota(examId, Difficulty.MEDIUM, mediumQuota);
        validateTierQuota(examId, Difficulty.HARD, hardQuota);
        validateTierPointRequiresTierQuota(request);
        requirePointPositiveWhenQuotaPositive(request);
    }

    private void requirePointPositiveWhenQuotaPositive(UpdateSessionRulesRequest request) {
        requirePointPositiveWhenQuotaPositive(request.getEasyQuota(), request.getEasyQuotaPoint(), "easy");
        requirePointPositiveWhenQuotaPositive(request.getMediumQuota(), request.getMediumQuotaPoint(), "medium");
        requirePointPositiveWhenQuotaPositive(request.getHardQuota(), request.getHardQuotaPoint(), "hard");
    }

    private void requirePointPositiveWhenQuotaPositive(int quota, Double point, String tierName) {
        if (quota > 0 && toPoints(point).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    tierName + " quota point must be greater than zero when " + tierName + " quota is greater than zero"
            );
        }
    }

    private void validateTierPointRequiresTierQuota(UpdateSessionRulesRequest request) {
        requirePointZeroWhenQuotaZero(request.getEasyQuota(), request.getEasyQuotaPoint(), "easy");
        requirePointZeroWhenQuotaZero(request.getMediumQuota(), request.getMediumQuotaPoint(), "medium");
        requirePointZeroWhenQuotaZero(request.getHardQuota(), request.getHardQuotaPoint(), "hard");
    }

    private void requirePointZeroWhenQuotaZero(int quota, Double point, String tierName) {
        if (quota == 0 && toPoints(point).compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    tierName + " quota point must be zero when " + tierName + " quota is zero"
            );
        }
    }

    private void validateRandomTierPoints(UpdateSessionRulesRequest request, BigDecimal totalExamPoint) {
        BigDecimal calculatedTotal = BigDecimal.ZERO
                .add(pointsForQuota(request.getEasyQuota(), request.getEasyQuotaPoint()))
                .add(pointsForQuota(request.getMediumQuota(), request.getMediumQuotaPoint()))
                .add(pointsForQuota(request.getHardQuota(), request.getHardQuotaPoint()));
        assertTotalPointsMatch(totalExamPoint, calculatedTotal);
    }

    private void validateRandomFlatQuotas(String examId, UpdateSessionRulesRequest request) {
        int questionQuota = request.getQuestionQuota();
        if (questionQuota <= 0) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    "Question quota must be greater than zero when difficulty tiers are disabled"
            );
        }

        int availableProblems = problemRepository.countByExam_ExamIdAndEnabledTrue(examId);
        if (questionQuota > availableProblems) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    "Question quota exceeds available problems in the exam bank"
            );
        }

        if (toPoints(request.getQuestionQuotaPoint()).compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    "Question quota point must be greater than zero when question quota is greater than zero"
            );
        }
    }

    private void validateRandomFlatPoints(UpdateSessionRulesRequest request, BigDecimal totalExamPoint) {
        BigDecimal calculatedTotal = pointsForQuota(request.getQuestionQuota(), request.getQuestionQuotaPoint());
        assertTotalPointsMatch(totalExamPoint, calculatedTotal);
    }

    private void validateEnabledProblemsHaveDifficulty(String examId) {
        if (problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficultyIsNull(examId) > 0) {
            throw new BadRequestException(
                    "UNTAGGED_PROBLEMS",
                    "All enabled problems must have a difficulty when difficulty tiers are used for points"
            );
        }
    }

    private void validateFixedTierPoints(String examId,
                                         UpdateSessionRulesRequest request,
                                         BigDecimal totalExamPoint) {
        int easyCount = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, Difficulty.EASY);
        int mediumCount = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, Difficulty.MEDIUM);
        int hardCount = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, Difficulty.HARD);

        if (easyCount + mediumCount + hardCount <= 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    "Exam must contain at least one enabled problem with a difficulty tier"
            );
        }

        requirePointPositiveWhenBankCountPositive(easyCount, request.getEasyQuotaPoint(), "easy");
        requirePointPositiveWhenBankCountPositive(mediumCount, request.getMediumQuotaPoint(), "medium");
        requirePointPositiveWhenBankCountPositive(hardCount, request.getHardQuotaPoint(), "hard");
        requirePointZeroWhenBankCountZero(easyCount, request.getEasyQuotaPoint(), "easy");
        requirePointZeroWhenBankCountZero(mediumCount, request.getMediumQuotaPoint(), "medium");
        requirePointZeroWhenBankCountZero(hardCount, request.getHardQuotaPoint(), "hard");

        BigDecimal calculatedTotal = BigDecimal.ZERO
                .add(pointsForQuota(easyCount, request.getEasyQuotaPoint()))
                .add(pointsForQuota(mediumCount, request.getMediumQuotaPoint()))
                .add(pointsForQuota(hardCount, request.getHardQuotaPoint()));
        assertTotalPointsMatch(totalExamPoint, calculatedTotal);
    }

    private void validateFixedFlatPoints(String examId,
                                         UpdateSessionRulesRequest request,
                                         BigDecimal totalExamPoint) {
        int enabledProblems = problemRepository.countByExam_ExamIdAndEnabledTrue(examId);
        if (enabledProblems <= 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    "Exam must contain at least one enabled problem"
            );
        }

        if (toPoints(request.getQuestionQuotaPoint()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    "Question quota point must be greater than zero"
            );
        }

        BigDecimal calculatedTotal = pointsForQuota(enabledProblems, request.getQuestionQuotaPoint());
        assertTotalPointsMatch(totalExamPoint, calculatedTotal);
    }

    private void requirePointPositiveWhenBankCountPositive(int bankCount, Double point, String tierName) {
        if (bankCount > 0 && toPoints(point).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    tierName + " quota point must be greater than zero when the exam contains "
                            + tierName + " problems"
            );
        }
    }

    private void requirePointZeroWhenBankCountZero(int bankCount, Double point, String tierName) {
        if (bankCount == 0 && toPoints(point).compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    tierName + " quota point must be zero when the exam contains no "
                            + tierName + " problems"
            );
        }
    }

    private void requireZeroPoint(Double point, String fieldName) {
        if (toPoints(point).compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException(
                    "INVALID_POINTS",
                    "Field '" + fieldName + "' must be zero for the selected rules configuration"
            );
        }
    }

    private BigDecimal pointsForQuota(int quota, Double pointPerQuota) {
        return toPoints(pointPerQuota)
                .multiply(BigDecimal.valueOf(quota))
                .setScale(POINT_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal toPoints(Double value) {
        return new BigDecimal(String.valueOf(value)).setScale(POINT_SCALE, RoundingMode.HALF_UP);
    }

    private double fromPoints(Double value) {
        return toPoints(value).doubleValue();
    }

    private void persistSelectionQuotas(ExamSession examSession, String examId, UpdateSessionRulesRequest request) {
        if (request.getSelectionMode() == SelectionMode.FIXED) {
            persistFixedSelectionQuotas(examSession, examId, request);
            return;
        }

        persistRandomSelectionQuotas(examSession, request);
    }

    private void persistFixedSelectionQuotas(ExamSession examSession,
                                             String examId,
                                             UpdateSessionRulesRequest request) {
        if (Boolean.TRUE.equals(request.getUseDifficultyTiers())) {
            int easyCount = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, Difficulty.EASY);
            int mediumCount = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, Difficulty.MEDIUM);
            int hardCount = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, Difficulty.HARD);

            examSession.setEasyQuota(easyCount);
            examSession.setMediumQuota(mediumCount);
            examSession.setHardQuota(hardCount);
            examSession.setQuestionQuota(easyCount + mediumCount + hardCount);
            return;
        }

        int enabledProblems = problemRepository.countByExam_ExamIdAndEnabledTrue(examId);
        examSession.setQuestionQuota(enabledProblems);
        examSession.setEasyQuota(0);
        examSession.setMediumQuota(0);
        examSession.setHardQuota(0);
    }

    private void persistRandomSelectionQuotas(ExamSession examSession, UpdateSessionRulesRequest request) {
        if (Boolean.TRUE.equals(request.getUseDifficultyTiers())) {
            examSession.setEasyQuota(request.getEasyQuota());
            examSession.setMediumQuota(request.getMediumQuota());
            examSession.setHardQuota(request.getHardQuota());
            examSession.setQuestionQuota(
                    request.getEasyQuota() + request.getMediumQuota() + request.getHardQuota()
            );
            return;
        }

        examSession.setQuestionQuota(request.getQuestionQuota());
        examSession.setEasyQuota(0);
        examSession.setMediumQuota(0);
        examSession.setHardQuota(0);
    }

    private void persistPointSettings(ExamSession examSession, UpdateSessionRulesRequest request) {
        if (Boolean.TRUE.equals(request.getUseDifficultyTiers())) {
            examSession.setQuestionQuotaPoint(0.0);
            examSession.setEasyQuotaPoint(fromPoints(request.getEasyQuotaPoint()));
            examSession.setMediumQuotaPoint(fromPoints(request.getMediumQuotaPoint()));
            examSession.setHardQuotaPoint(fromPoints(request.getHardQuotaPoint()));
            return;
        }

        examSession.setQuestionQuotaPoint(fromPoints(request.getQuestionQuotaPoint()));
        examSession.setEasyQuotaPoint(0.0);
        examSession.setMediumQuotaPoint(0.0);
        examSession.setHardQuotaPoint(0.0);
    }

    private void assertTotalPointsMatch(BigDecimal expectedTotal, BigDecimal actualTotal) {
        if (expectedTotal.compareTo(actualTotal) != 0) {
            throw new BadRequestException(
                    "UNMATCHED_TOTAL_POINT",
                    "Sum of quota and point settings does not match total exam point "
                            + "(expected total: " + expectedTotal + ", actual total: " + actualTotal + ")"
            );
        }
    }

    private void validateTierQuota(String examId, Difficulty difficulty, int quota) {
        if (quota == 0) {
            return;
        }

        int available = problemRepository.countByExam_ExamIdAndEnabledTrueAndDifficulty(examId, difficulty);
        if (quota > available) {
            throw new BadRequestException(
                    "INVALID_QUOTAS",
                    difficulty.name().toLowerCase() + " quota exceeds available "
                            + difficulty.name().toLowerCase() + " problems in the exam bank"
            );
        }
    }


    private TeacherExamSessionDetailsResponse toTeacherExamSessionDetailsResponse(ExamSession examSession, Exam exam,
                                                                                  List<ExamSessionLanguage> examSessionLanguages,
                                                                                  GroupDetails groupDetails) {
        return TeacherExamSessionDetailsResponse.builder()
                .examSessionId(examSession.getExamSessionId())
                .examSessionTitle(examSession.getExamSessionTitle())
                .examId(exam.getExamId())
                .examTitle(exam.getTitle())
                .groupId(groupDetails.getGroupId())
                .groupName(groupDetails.getName())
                .startsAt(examSession.getStartsAt())
                .endsAt(examSession.getEndsAt())
                .status(examSession.getStatus())
                .publishReady(exam.getPublishReady())
                .selectionMode(examSession.getSelectionMode())
                .useDifficultyTiers(examSession.getUseDifficultyTiers())
                .totalExamPoint(examSession.getTotalExamPoint())
                .questionQuota(examSession.getQuestionQuota())
                .questionQuotaPoint(examSession.getQuestionQuotaPoint())
                .easyQuota(examSession.getEasyQuota())
                .easyQuotaPoint(examSession.getEasyQuotaPoint())
                .mediumQuota(examSession.getMediumQuota())
                .mediumQuotaPoint(examSession.getMediumQuotaPoint())
                .hardQuota(examSession.getHardQuota())
                .hardQuotaPoint(examSession.getHardQuotaPoint())
                .maxQuestionChanges(examSession.getMaxQuestionChanges())
                .maxCheatEvents(examSession.getMaxCheatEvents())
                .cheatBlockMode(examSession.getCheatBlockMode())
                .createdAt(examSession.getCreatedAt())
                .updatedAt(examSession.getUpdatedAt())
                .programmingLanguages(
                        examSessionLanguages.stream()
                                .map(esl -> programmingLanguageMapper.toProgrammingLanguageResponse(esl.getLanguage()))
                                .toList()
                )
                .build();
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
