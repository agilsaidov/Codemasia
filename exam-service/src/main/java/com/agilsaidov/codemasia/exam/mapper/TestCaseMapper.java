package com.agilsaidov.codemasia.exam.mapper;

import com.agilsaidov.codemasia.exam.dto.request.CreateTestCaseRequest;
import com.agilsaidov.codemasia.exam.dto.response.TestCaseResponse;
import com.agilsaidov.codemasia.exam.model.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {

    @Mapping(target = "problem", ignore = true)
    @Mapping(target = "testCaseId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "stdin", ignore = true)
    @Mapping(target = "sample", ignore = true)
    @Mapping(target = "weight", ignore = true)
    TestCase fromCreateTestCaseRequest(CreateTestCaseRequest request);

    TestCaseResponse toTestCaseResponse(TestCase testCase);
}
