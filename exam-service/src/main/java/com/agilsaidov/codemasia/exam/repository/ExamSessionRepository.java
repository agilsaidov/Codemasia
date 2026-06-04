package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.ExamSession;
import com.agilsaidov.codemasia.exam.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    List<ExamSession> findAllByExam_ExamIdAndStatus(String examId, SessionStatus status);

    boolean existsByExam_ExamIdAndStatus(String examId, SessionStatus status);

    long countByExam_ExamId(String examId);
}
