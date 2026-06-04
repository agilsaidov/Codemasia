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

        if (examSessionRepository.existsByExam_ExamIdAndStatus(examId, SessionStatus.ACTIVE)) {
            log.warn("Delete blocked for exam [{}]: one or more sessions are currently ACTIVE", examId);
            throw new BadRequestException(
                    "ACTIVE_SESSIONS_EXIST",
                    "Cannot delete exam [" + examId + "] while sessions are active"
            );
        }

        List<ExamSession> scheduled = examSessionRepository
                .findAllByExam_ExamIdAndStatus(examId, SessionStatus.SCHEDULED);

        for (ExamSession session : scheduled) {
            session.setStatus(SessionStatus.CANCELLED);
            session.setEnabled(false);
        }

        if (!scheduled.isEmpty()) {
            examSessionRepository.saveAll(scheduled);
        }

        log.info("Exam [{}] cascade complete: {} SCHEDULED session(s) cancelled", examId, scheduled.size());
        return new SessionCascadeResult(scheduled.size());
    }

    @Transactional(readOnly = true)
    public long countByExamId(String examId) {
        return examSessionRepository.countByExam_ExamId(examId);
    }

    public record SessionCascadeResult(int cancelled) {}
}
