package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.response.ProblemResponse;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ProblemMapper;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.model.Problem;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ExamRepository examRepository;
    private final ProblemMapper problemMapper;
    private final ProblemRepository problemRepository;


    public ProblemResponse createProblem(String examId, String role, UUID creatorId, CreateProblemRequest request){
        Exam exam = role.equals("ADMIN") ? getExam(examId) : getOwnedEnabledExam(creatorId, examId);
        Problem problem = problemMapper.fromCreateProblemRequestToProblem(request);
        problem.setExam(exam);
        return problemMapper.toProblemResponse(problemRepository.save(problem));
    }


    //Helper Methods
    private Exam getOwnedEnabledExam(UUID creatorId, String examId) {
        Exam exam = getExam(examId);

        if (!creatorId.equals(exam.getCreatorId())) {
            throw new ForbiddenException("FORBIDDEN_ACTION", "You are not allowed to access this resource");
        }

        if (!Boolean.TRUE.equals(exam.getEnabled())) {
            throw new NotFoundException(
                    "EXAM_NOT_FOUND",
                    "Exam with id " + examId + " not found"
            );
        }

        return exam;
    }

    private Exam getExam(String examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(
                        "EXAM_NOT_FOUND",
                        "Exam with id " + examId + " not found")
                );
    }
}
