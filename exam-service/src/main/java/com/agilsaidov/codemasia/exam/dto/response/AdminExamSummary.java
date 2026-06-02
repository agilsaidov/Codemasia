package com.agilsaidov.codemasia.exam.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter
public class AdminExamSummary {
    private String examId;
    private String title;
    private Boolean enabled;
    private Boolean publishReady;
    private UUID creatorId;
    private OffsetDateTime createdAt;
}
