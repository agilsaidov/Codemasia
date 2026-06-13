package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    int countByProblem_ProblemId(Long problemId);

    List<TestCase> findByProblem_ProblemIdOrderByPositionAsc(Long problemId);

    Optional<TestCase> findByTestCaseIdAndProblem_ProblemId(Long testCaseId, Long problemId);
}
