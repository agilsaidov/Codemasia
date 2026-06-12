package com.agilsaidov.codemasia.exam.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class TestCaseResponse {
    private Long testCaseId;
    private String stdin;
    private String expectedOutput;
    private Boolean sample;
    private Integer position;
    private OffsetDateTime createdAt;
}
