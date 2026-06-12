package com.agilsaidov.codemasia.exam.repository;

import com.agilsaidov.codemasia.exam.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    int countByProblem_ProblemId(Long problemId);

    List<TestCase> findByProblem_ProblemIdOrderByPositionAsc(Long problemId);
}
