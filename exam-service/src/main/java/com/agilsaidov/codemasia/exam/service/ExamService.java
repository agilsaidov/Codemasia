package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamRequest;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.exception.BadRequestException;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import com.agilsaidov.codemasia.exam.mapper.ExamMapper;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
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

        return examMapper.toTeacherExamDetailsResponse(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public Page<TeacherExamDetailsResponse> getTeacherExams(UUID creatorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return examRepository.findAllByCreatorId(creatorId, pageable)
                .map(examMapper::toTeacherExamDetailsResponse);
    }

    @Transactional
    public void toggleExamPublishReady(UUID creatorId, String examId, boolean publishReady) {
        Exam exam = getOwnedExam(creatorId, examId);

        if (publishReady && !Boolean.TRUE.equals(exam.getEnabled())) {
            throw new BadRequestException(
                    "EXAM_DISABLED",
                    "Cannot mark a disabled exam as publish ready"
            );
        }

        exam.setPublishReady(publishReady);
        examRepository.save(exam);
    }

    @Transactional
    public void enableExam(UUID creatorId, String examId, boolean enabled) {
        Exam exam = getOwnedExam(creatorId, examId);
        exam.setEnabled(enabled);

        if (!enabled) {
            exam.setPublishReady(false);
        }

        examRepository.save(exam);
    }

    private Exam getOwnedExam(UUID creatorId, String examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException(
                        "EXAM_NOT_FOUND",
                        "Exam with id " + examId + " not found")
                );

        if (!creatorId.equals(exam.getCreatorId())) {
            throw new ForbiddenException("FORBIDDEN_ACTION", "You are not allowed to modify this exam");
        }

        return exam;
    }
}
