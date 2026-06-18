package com.agilsaidov.codemasia.exam.controller;

import com.agilsaidov.codemasia.exam.dto.response.ProgrammingLanguageResponse;
import com.agilsaidov.codemasia.exam.service.ProgrammingLanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/programming-languages")
@RequiredArgsConstructor
public class ProgrammingLanguageController {

    private final ProgrammingLanguageService programmingLanguageService;

    @GetMapping
    public ResponseEntity<List<ProgrammingLanguageResponse>> getProgrammingLanguages(){
        return ResponseEntity.ok(programmingLanguageService.getProgrammingLanguages());
    }
}
