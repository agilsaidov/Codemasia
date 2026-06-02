package com.agilsaidov.codemasia.exam.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter
public class AdminExamDetailsResponse {
    private String examId;
    private String title;
    private String description;
    private Boolean publishReady;
    private Boolean enabled;
    private UUID creatorId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int problemCount;
    private long sessionCount;
}
