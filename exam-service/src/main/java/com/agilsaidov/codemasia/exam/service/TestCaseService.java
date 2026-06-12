package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateTestCaseRequest;
import com.agilsaidov.codemasia.exam.dto.response.TestCaseResponse;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.TestCaseMapper;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.model.Problem;
import com.agilsaidov.codemasia.exam.model.TestCase;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ProblemRepository;
import com.agilsaidov.codemasia.exam.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseMapper testCaseMapper;
    private final TestCaseRepository testCaseRepository;

    @Transactional
    public TestCaseResponse createTestCase(String examId, String role, UUID creatorId, Long problemId,
                                           CreateTestCaseRequest request) {
        log.debug("Creating test case for problem={} in exam={} by creator={} role={}", problemId, examId, creatorId, role);

        Problem problem = role.equals("TEACHER")
                ? getOwnedEnabledProblem(creatorId, examId, problemId)
                : getExamProblem(examId, problemId);

        TestCase testCase = testCaseMapper.fromCreateTestCaseRequest(request);
        testCase.setProblem(problem);
        testCase.setStdin(request.getStdin() != null ? request.getStdin() : "");
        testCase.setSample(request.getSample() != null ? request.getSample() : false);
        testCase.setPosition(request.getPosition() != null
                ? request.getPosition()
                : testCaseRepository.countByProblem_ProblemId(problemId));

        TestCase saved = testCaseRepository.save(testCase);
        log.info("TestCase={} created for problem={} in exam={} by creator={} role={}",
                saved.getTestCaseId(), problemId, examId, creatorId, role);
        return testCaseMapper.toTestCaseResponse(saved);
    }



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

    private Problem getOwnedEnabledProblem(UUID creatorId, String examId, Long problemId) {
        getOwnedEnabledExam(creatorId, examId);
        Problem problem = problemRepository
                .getProblemByExam_ExamIdAndProblemId(examId, problemId)
                .orElseThrow(() -> {
                    log.warn("Problem={} not found in exam={}", problemId, examId);
                    return new NotFoundException(
                            "PROBLEM_NOT_FOUND",
                            "Problem with id " + problemId + " not found"
                    );
                });

        if (!Boolean.TRUE.equals(problem.getEnabled())) {
            log.warn("Problem={} in exam={} is disabled, requested by creator={}", problemId, examId, creatorId);
            throw new NotFoundException(
                    "PROBLEM_NOT_FOUND",
                    "Problem with id " + problemId + " not found"
            );
        }
        return problem;
    }

    private Problem getExamProblem(String examId, Long problemId) {
        getExam(examId);
        return problemRepository.getProblemByExam_ExamIdAndProblemId(examId, problemId)
                .orElseThrow(() -> {
                    log.warn("Problem={} not found in exam={}", problemId, examId);
                    return new NotFoundException(
                            "PROBLEM_NOT_FOUND",
                            "Problem with id " + problemId + " not found"
                    );
                });
    }
}
