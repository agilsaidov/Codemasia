package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    int countByExam_ExamId(String examId);
}
