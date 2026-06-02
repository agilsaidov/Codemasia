package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.model.ExamSession;
import com.agilsaidov.codemasia.exam.model.SessionStatus;
import com.agilsaidov.codemasia.exam.repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final ExamSessionRepository examSessionRepository;

    @Transactional
    public SessionCascadeResult cascadeOnExamDelete(Exam exam) {
        OffsetDateTime now = OffsetDateTime.now();
        int cancelled = 0;
        int closing = 0;
        int unchanged = 0;
        List<ExamSession> modified = new ArrayList<>();

        for (ExamSession session : examSessionRepository.findAllByExam_ExamId(exam.getExamId())) {
            if (session.getStatus() == SessionStatus.FINISHED || session.getStatus() == SessionStatus.CANCELLED) {
                unchanged++;
                continue;
            }

            switch (resolveLifecyclePhase(session, now)) {
                case SCHEDULED -> {
                    session.setStatus(SessionStatus.CANCELLED);
                    session.setEnabled(false);
                    modified.add(session);
                    cancelled++;
                }
                case ACTIVE -> {
                    session.setStatus(SessionStatus.CLOSING);
                    modified.add(session);
                    closing++;
                }
                case FINISHED -> {
                    session.setStatus(SessionStatus.FINISHED);
                    modified.add(session);
                    unchanged++;
                }
            }
        }

        if (!modified.isEmpty()) {
            examSessionRepository.saveAll(modified);
        }

        return new SessionCascadeResult(cancelled, closing, unchanged);
    }


    private LifecyclePhase resolveLifecyclePhase(ExamSession session, OffsetDateTime now) {
        if (session.getEndsAt() != null && !session.getEndsAt().isAfter(now)) {
            return LifecyclePhase.FINISHED;
        }
        if (session.getStartsAt() != null && session.getStartsAt().isAfter(now)) {
            return LifecyclePhase.SCHEDULED;
        }
        return LifecyclePhase.ACTIVE;
    }

    public record SessionCascadeResult(int cancelled, int closing, int unchanged) {
    }

    private enum LifecyclePhase {
        SCHEDULED,
        ACTIVE,
        FINISHED
    }
}
