package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamRequest;
import com.agilsaidov.codemasia.exam.dto.request.UpdateExamRequest;
import com.agilsaidov.codemasia.exam.dto.response.AdminExamDetailsResponse;
import com.agilsaidov.codemasia.exam.dto.response.AdminExamSummary;
import com.agilsaidov.codemasia.exam.dto.response.DeleteExamResponse;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamSummary;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ExamMapper;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.repository.ProblemRepository;
import com.agilsaidov.codemasia.exam.specification.ExamSpec;
import com.agilsaidov.codemasia.exam.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private static final int ID_GEN_MAX_ATTEMPTS = 5;

    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository;
    private final ExamSessionService examSessionService;
    private final IdGenerator idGenerator;
    private final ExamMapper examMapper;

    @Transactional
    public TeacherExamDetailsResponse createExam(CreateExamRequest request, UUID creatorId) {
        log.debug("Creating exam by creator={}", creatorId);

        String examId = generateUniqueExamId();

        Exam exam = Exam.builder()
                .examId(examId)
                .creatorId(creatorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Exam saved = examRepository.save(exam);
        log.info("Exam={} created by creator={}", saved.getExamId(), creatorId);
        return enrichTeacherDetails(saved);
    }


    @Transactional(readOnly = true)
    public Page<TeacherExamSummary> getTeacherExams(UUID creatorId, int page, int size) {
        log.debug("Fetching exams for creator={} page={} size={}", creatorId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TeacherExamSummary> result = examRepository.findAllByCreatorIdAndEnabledTrue(creatorId, pageable)
                .map(examMapper::toTeacherExamSummary);
        log.debug("Fetched {} exam(s) for creator={}", result.getTotalElements(), creatorId);
        return result;
    }


    @Transactional(readOnly = true)
    public Page<AdminExamSummary> getExams(String title, UUID creatorId, Boolean enabled, int page, int size) {
        log.debug("Fetching exams (admin): title={} creatorId={} enabled={} page={} size={}", title, creatorId, enabled, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AdminExamSummary> result = examRepository.findAll(ExamSpec.withFilters(title, creatorId, enabled), pageable)
                .map(examMapper::toAdminExamSummary);
        log.debug("Fetched {} exam(s) (admin): title={} creatorId={} enabled={}", result.getTotalElements(), title, creatorId, enabled);
        return result;
    }


    @Transactional(readOnly = true)
    public TeacherExamDetailsResponse getTeacherExamDetails(UUID creatorId, String examId) {
        log.debug("Fetching exam={} details for teacher={}", examId, creatorId);
        Exam exam = getOwnedEnabledExam(creatorId, examId);
        TeacherExamDetailsResponse response = enrichTeacherDetails(exam);
        log.debug("Fetched exam={} details for teacher={}", examId, creatorId);
        return response;
    }


    @Transactional(readOnly = true)
    public AdminExamDetailsResponse getAdminExamDetails(String examId) {
        log.debug("Fetching exam={} details (admin)", examId);
        Exam exam = getExam(examId);
        AdminExamDetailsResponse response = enrichAdminExamDetails(exam);
        log.debug("Fetched exam={} details (admin)", examId);
        return response;
    }


    @Transactional
    public AdminExamDetailsResponse updateAdminExam(String examId, UpdateExamRequest request) {
        log.debug("Updating exam={} (admin)", examId);
        Exam exam = getExam(examId);

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setPublishReady(false);
        examRepository.save(exam);

        log.info("Exam={} updated (admin)", examId);
        return enrichAdminExamDetails(exam);
    }


    @Transactional
    public TeacherExamDetailsResponse updateTeacherExam(UUID teacherId, String examId, UpdateExamRequest request) {
        log.debug("Updating exam={} by teacher={}", examId, teacherId);
        Exam exam = getOwnedEnabledExam(teacherId, examId);

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        examRepository.save(exam);

        log.info("Exam={} updated by teacher={}", examId, teacherId);
        return enrichTeacherDetails(exam);
    }


    @Transactional
    public DeleteExamResponse deleteExam(UUID creatorId, String examId) {
        log.debug("Deleting exam={} by teacher={}", examId, creatorId);
        Exam exam = getOwnedEnabledExam(creatorId, examId);
        return softDeleteExam(exam);
    }


    @Transactional
    public void enableExam(String examId, boolean enabled) {
        log.debug("Setting enabled={} for exam={} (admin)", enabled, examId);
        Exam exam = getExam(examId);

        if (enabled) {
            if (Boolean.TRUE.equals(exam.getEnabled())) {
                log.debug("Exam={} is already enabled, no-op", examId);
                return;
            }
            exam.setEnabled(true);
            examRepository.save(exam);
            log.info("Exam={} re-enabled by admin", examId);
            return;
        }

        if (!Boolean.TRUE.equals(exam.getEnabled())) {
            log.debug("Exam={} is already disabled, no-op", examId);
            return;
        }

        log.info("Admin requested disable (soft-delete) for exam={}", examId);
        softDeleteExam(exam);
    }


    @Transactional
    public void toggleExamPublishReady(UUID creatorId, String examId, boolean publishReady) {
        log.debug("Toggling publishReady={} for exam={} by creator={}", publishReady, examId, creatorId);
        Exam exam = getOwnedEnabledExam(creatorId, examId);
        exam.setPublishReady(publishReady);
        examRepository.save(exam);
        log.info("Exam={} publishReady set to {} by creator={}", examId, publishReady, creatorId);
    }


    // Helper methods

    private DeleteExamResponse softDeleteExam(Exam exam) {
        ExamSessionService.SessionCascadeResult cascade = examSessionService.cascadeOnExamDelete(exam);

        exam.setEnabled(false);
        exam.setPublishReady(false);
        examRepository.save(exam);
        log.info("Exam={} soft-deleted: {} scheduled session(s) cancelled", exam.getExamId(), cascade.cancelled());

        DeleteExamResponse response = new DeleteExamResponse();
        response.setExamId(exam.getExamId());
        response.setDeleted(true);
        response.setSessionsCancelled(cascade.cancelled());
        return response;
    }

    private TeacherExamDetailsResponse enrichTeacherDetails(Exam exam) {
        TeacherExamDetailsResponse response = examMapper.toTeacherExamDetailsResponse(exam);
        response.setProblemCount(problemRepository.countByExam_ExamId(exam.getExamId()));
        return response;
    }

    private AdminExamDetailsResponse enrichAdminExamDetails(Exam exam) {
        AdminExamDetailsResponse response = examMapper.toAdminExamDetailsResponse(exam);
        response.setProblemCount(problemRepository.countByExam_ExamId(exam.getExamId()));
        response.setSessionCount(examSessionService.countByExamId(exam.getExamId()));
        return response;
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

    private String generateUniqueExamId() {
        for (int attempt = 1; attempt <= ID_GEN_MAX_ATTEMPTS; attempt++) {
            String examId = idGenerator.generateExamId();
            if (!examRepository.existsByExamId(examId)) {
                return examId;
            }
            log.warn("Exam ID collision on attempt {}/{}: [{}]", attempt, ID_GEN_MAX_ATTEMPTS, examId);
        }
        log.error("Failed to generate unique exam ID after {} attempts", ID_GEN_MAX_ATTEMPTS);
        throw new IllegalStateException(
                "Could not generate a unique exam ID after " + ID_GEN_MAX_ATTEMPTS + " attempts"
        );
    }
}
