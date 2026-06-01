package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.request.CreateExamRequest;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.mapper.ExamMapper;
import com.agilsaidov.codemasia.exam.model.Exam;
import com.agilsaidov.codemasia.exam.repository.ExamRepository;
import com.agilsaidov.codemasia.exam.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
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
}
