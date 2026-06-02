package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {

    List<ExamSession> findAllByExam_ExamId(String examId);

    long countByExam_ExamId(String examId);
}
