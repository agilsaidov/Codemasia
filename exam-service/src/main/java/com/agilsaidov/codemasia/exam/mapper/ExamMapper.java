package com.agilsaidov.codemasia.exam.mapper;

import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.model.Exam;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExamMapper {

    TeacherExamDetailsResponse toTeacherExamDetailsResponse(Exam exam);
}
