package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.Difficulty;
import com.agilsaidov.codemasia.exam.model.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem> {
    int countByExam_ExamId(String examId);

    int countByExam_ExamIdAndEnabledTrue(String examId);

    int countByExam_ExamIdAndEnabledTrueAndDifficulty(String examId, Difficulty difficulty);

    int countByExam_ExamIdAndEnabledTrueAndDifficultyIsNull(String examId);

    Page<Problem> findAll(Specification<Problem> problemSpecification, Pageable pageable);

    Optional<Problem> getProblemByExam_ExamIdAndProblemId(String examId, Long problemId);
}
