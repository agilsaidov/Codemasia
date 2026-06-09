package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.response.ProblemResponse;
import com.agilsaidov.codemasia.exam.dto.response.ProblemSummary;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ProblemMapper;
import com.agilsaidov.codemasia.exam.model.Difficulty;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.model.Problem;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ProblemRepository;
import com.agilsaidov.codemasia.exam.specification.ProblemSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ExamRepository examRepository;
    private final ProblemMapper problemMapper;
    private final ProblemRepository problemRepository;


    @Transactional
    public ProblemResponse createProblem(String examId, String role, UUID creatorId, CreateProblemRequest request){
        log.debug("Creating problem in exam={} by creator={} role={}", examId, creatorId, role);
        Exam exam = role.equals("ADMIN") ? getExam(examId) : getOwnedEnabledExam(creatorId, examId);
        Problem problem = problemMapper.fromCreateProblemRequestToProblem(request);
        problem.setExam(exam);
        Problem saved = problemRepository.save(problem);
        log.info("Problem={} created in exam={} by creator={} role={}", saved.getProblemId(), examId, creatorId, role);
        return problemMapper.toProblemResponse(saved);
    }


    @Transactional(readOnly = true)
    public Page<ProblemSummary> getProblems(String examId, UUID creatorId, String role, String title,
                                            Difficulty difficulty, OffsetDateTime createdAt,
                                            Integer point, int page, int size) {

        log.debug("Fetching problems: exam={} role={} creatorId={} title={} difficulty={} point={} page={} size={}",
                examId, role, creatorId, title, difficulty, point, page, size);

        if (role.equals("TEACHER")) {
            getOwnedEnabledExam(creatorId, examId);
        } else {
            getExam(examId);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProblemSummary> result = problemRepository
                .findAll(ProblemSpec.withFilters(examId, title, difficulty, createdAt, point), pageable)
                .map(problemMapper::toProblemSummary);
        log.debug("Fetched {} problem(s) for exam={} by creator={} role={}", result.getTotalElements(), examId, creatorId, role);
        return result;
    }


    @Transactional(readOnly = true)
    public ProblemResponse getProblemDetails(UUID creatorId, String role, String examId, Long problemId) {
        log.debug("Fetching problem details: exam={} problem={} role={} creatorId={}", examId, problemId, role, creatorId);

        ProblemResponse response;
        if (role.equals("TEACHER")) {
            response = problemMapper.toProblemResponse(getOwnedEnabledExamProblem(creatorId, examId, problemId));
        } else {
            response = problemMapper.toProblemResponse(getExamProblem(examId, problemId));
        }
        log.debug("Fetched problem={} from exam={} for creator={} role={}", problemId, examId, creatorId, role);
        return response;
    }


    @Transactional
    public ProblemResponse updateProblem(UUID creatorId, String role,
                                         String examId, Long problemId,
                                         UpdateProblemRequest request){

        log.debug("Updating problem={} in exam={} by user={} role={}", problemId, examId, creatorId, role);

        Problem problem = role.equals("TEACHER")
                ? getOwnedEnabledExamProblem(creatorId, examId, problemId)
                : getExamProblem(examId, problemId);

        Integer previousPoint = problem.getPoint();
        Integer previousTimeLimitMs = problem.getTimeLimitMs();
        Integer previousMemoryLimitKb = problem.getMemoryLimitKb();
        Difficulty previousDifficulty = problem.getDifficulty();

        problem.setTitle(request.getTitle());
        problem.setStatement(request.getStatement());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitKb(request.getMemoryLimitKb());
        problem.setPoint(request.getPoint());
        problem.setDifficulty(request.getDifficulty());

        Problem saved = problemRepository.save(problem);
        log.info("Problem={} updated in exam={} by user={} role={}", problemId, examId, creatorId, role);

        if (!Objects.equals(previousPoint, saved.getPoint())
                || !Objects.equals(previousTimeLimitMs, saved.getTimeLimitMs())
                || !Objects.equals(previousMemoryLimitKb, saved.getMemoryLimitKb())
                || previousDifficulty != saved.getDifficulty()) {
            log.info(
                    "Problem={} scoring/limits changed in exam={} by user={} role={}: point {} -> {}, timeLimitMs {} -> {}, memoryLimitKb {} -> {}, difficulty {} -> {}",
                    problemId, examId, creatorId, role,
                    previousPoint, saved.getPoint(),
                    previousTimeLimitMs, saved.getTimeLimitMs(),
                    previousMemoryLimitKb, saved.getMemoryLimitKb(),
                    previousDifficulty, saved.getDifficulty()
            );
        }

        return problemMapper.toProblemResponse(saved);
    }


    //Helper Methods
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


    private Problem getOwnedEnabledExamProblem(UUID creatorId, String examId, Long problemId) {
        getOwnedEnabledExam(creatorId, examId);
        return problemRepository
                .getProblemByExam_ExamIdAndProblemId(examId, problemId)
                .orElseThrow(() -> {
                    log.warn("Problem={} not found in exam={}", problemId, examId);
                    return new NotFoundException(
                            "PROBLEM_NOT_FOUND",
                            "Problem with id " + problemId + " not found"
                    );
                });
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
