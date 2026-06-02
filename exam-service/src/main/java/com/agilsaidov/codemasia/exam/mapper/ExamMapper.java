package com.agilsaidov.codemasia.exam.mapper;

import com.agilsaidov.codemasia.exam.dto.response.AdminExamDetailsResponse;
import com.agilsaidov.codemasia.exam.dto.response.AdminExamSummary;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamDetailsResponse;
import com.agilsaidov.codemasia.exam.dto.response.TeacherExamSummary;
import com.agilsaidov.codemasia.exam.model.Exam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExamMapper {

    @Mapping(target = "problemCount", ignore = true)
    TeacherExamDetailsResponse toTeacherExamDetailsResponse(Exam exam);

    TeacherExamSummary toTeacherExamSummary(Exam exam);

    AdminExamSummary toAdminExamSummary(Exam exam);

    @Mapping(target = "problemCount", ignore = true)
    @Mapping(target = "sessionCount", ignore = true)
    AdminExamDetailsResponse toAdminExamDetailsResponse(Exam exam);
}
