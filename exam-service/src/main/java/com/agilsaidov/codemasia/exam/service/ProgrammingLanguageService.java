package com.agilsaidov.codemasia.exam.service;

import com.agilsaidov.codemasia.exam.dto.response.ProgrammingLanguageResponse;
import com.agilsaidov.codemasia.exam.mapper.ProgrammingLanguageMapper;
import com.agilsaidov.codemasia.exam.repository.ProgrammingLanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgrammingLanguageService {

    private final ProgrammingLanguageRepository repository;
    private final ProgrammingLanguageMapper mapper;

    public List<ProgrammingLanguageResponse> getProgrammingLanguages(){
        return repository.findAllByEnabled(true)
                .stream()
                .map(mapper::toProgrammingLanguageResponse)
                .toList();
    }
}
