package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamRequest;
import com.agilsaidov.codemasia.exam.dto.response.AdminExamDetailsResponse;
import com.agilsaidov.codemasia.exam.dto.response.AdminExamSummary;
import com.agilsaidov.codemasia.exam.dto.response.DeleteExamResponse;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamSummary;
import com.agilsaidov.codemasia.exam.exception.BadRequestException;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ExamMapper;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ExamSessionRepository;
import com.agilsaidov.codemasia.exam.repository.ProblemRepository;
import com.agilsaidov.codemasia.exam.specification.ExamSpec;
import com.agilsaidov.codemasia.exam.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamSessionRepository examSessionRepository;
    private final ProblemRepository problemRepository;
    private final ExamSessionService examSessionService;
    private final IdGenerator idGenerator;
    private final ExamMapper examMapper;

    @Transactional
    public TeacherExamDetailsResponse createExam(CreateExamRequest request, UUID creatorId) {
        String examId;

        do {
            examId = idGenerator.generateExamId();
        } while (examRepository.existsByExamId(examId));

        Exam exam = Exam.builder()
                .examId(examId)
                .creatorId(creatorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return enrichTeacherDetails(examRepository.save(exam));
    }


    @Transactional(readOnly = true)
    public Page<TeacherExamSummary> getTeacherExams(UUID creatorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return examRepository.findAllByCreatorIdAndEnabledTrue(creatorId, pageable)
                .map(examMapper::toTeacherExamSummary);
    }


    @Transactional(readOnly = true)
    public Page<AdminExamSummary> getExams(String title, UUID creatorId, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return examRepository.findAll(ExamSpec.withFilters(title, creatorId, enabled), pageable)
                .map(examMapper::toAdminExamSummary);
    }


    @Transactional(readOnly = true)
    public TeacherExamDetailsResponse getTeacherExamDetails(UUID creatorId, String examId) {
        Exam exam = getOwnedEnabledExam(creatorId, examId);
        return enrichTeacherDetails(exam);
    }


    @Transactional(readOnly = true)
    public AdminExamDetailsResponse getAdminExamDetails(String examId) {
        Exam exam = getExam(examId);
        AdminExamDetailsResponse response = examMapper.toAdminExamDetailsResponse(exam);
        response.setProblemCount(problemRepository.countByExam_ExamId(examId));
        response.setSessionCount(examSessionRepository.countByExam_ExamId(examId));
        return response;
    }


    @Transactional
    public DeleteExamResponse deleteExam(UUID creatorId, String examId) {
        Exam exam = getOwnedEnabledExam(creatorId, examId);
        return softDeleteExam(exam);
    }


    @Transactional
    public void enableExam(String examId, boolean enabled) {
        Exam exam = getExam(examId);

        if (enabled) {
            if (Boolean.TRUE.equals(exam.getEnabled())) {
                return;
            }
            exam.setEnabled(true);
            examRepository.save(exam);
            return;
        }

        if (!Boolean.TRUE.equals(exam.getEnabled())) {
            return;
        }

        softDeleteExam(exam);
    }


    @Transactional
    public void toggleExamPublishReady(UUID creatorId, String examId, boolean publishReady) {
        Exam exam = getOwnedEnabledExam(creatorId, examId);

        if (publishReady && !Boolean.TRUE.equals(exam.getEnabled())) {
            throw new BadRequestException(
                    "EXAM_DISABLED",
                    "Cannot mark a disabled exam as publish ready"
            );
        }

        exam.setPublishReady(publishReady);
        examRepository.save(exam);
    }



    //Helper Methods
    private DeleteExamResponse softDeleteExam(Exam exam) {
        exam.setEnabled(false);
        exam.setPublishReady(false);
        examRepository.save(exam);

        ExamSessionService.SessionCascadeResult cascade = examSessionService.cascadeOnExamDelete(exam);

        DeleteExamResponse response = new DeleteExamResponse();
        response.setExamId(exam.getExamId());
        response.setDeleted(true);
        response.setSessionsCancelled(cascade.cancelled());
        response.setSessionsClosing(cascade.closing());
        response.setSessionsUnchanged(cascade.unchanged());
        return response;
    }

    private TeacherExamDetailsResponse enrichTeacherDetails(Exam exam) {
        TeacherExamDetailsResponse response = examMapper.toTeacherExamDetailsResponse(exam);
        response.setProblemCount(problemRepository.countByExam_ExamId(exam.getExamId()));
        return response;
    }

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
