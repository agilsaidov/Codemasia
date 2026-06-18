package com.agilsaidov.codemasia.exam.mapper;

import com.agilsaidov.codemasia.exam.dto.response.ProgrammingLanguageResponse;
import com.agilsaidov.codemasia.exam.model.ProgrammingLanguage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProgrammingLanguageMapper {
    @Mapping(source = "judge0LanguageId", target = "id")
    @Mapping(source = "name", target = "languageName")
    ProgrammingLanguageResponse toProgrammingLanguageResponse(ProgrammingLanguage programmingLanguage);
}
