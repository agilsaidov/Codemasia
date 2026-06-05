package com.agilsaidov.codemasia.exam.mapper;

import com.agilsaidov.codemasia.exam.dto.request.CreateProblemRequest;
import com.agilsaidov.codemasia.exam.dto.response.ProblemResponse;
import com.agilsaidov.codemasia.exam.model.Problem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProblemMapper {
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "problemId",  ignore = true)
    Problem fromCreateProblemRequestToProblem(CreateProblemRequest request);

    @Mapping(source = "exam.examId", target = "examId")
    ProblemResponse toProblemResponse(Problem problem);
}
