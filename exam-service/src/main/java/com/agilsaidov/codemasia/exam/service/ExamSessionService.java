package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.exception.BadRequestException;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.model.ExamSession;
import com.agilsaidov.codemasia.exam.model.SessionStatus;
import com.agilsaidov.codemasia.exam.repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final ExamSessionRepository examSessionRepository;

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
}
