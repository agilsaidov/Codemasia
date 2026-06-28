package com.agilsaidov.codemasia.exam.dto.response;

import com.agilsaidov.codemasia.exam.model.CheatBlockMode;
import com.agilsaidov.codemasia.exam.model.SelectionMode;
import com.agilsaidov.codemasia.exam.model.SessionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
@Builder
public class TeacherExamSessionDetailsResponse {
    // identity
    private Long examSessionId;
    private String examSessionTitle;

    private String examId;
    private String examTitle;

    private String groupId;
    private String groupName;

    // schedule
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private SessionStatus status;
    private Boolean publishReady;

    // rules
    private SelectionMode selectionMode;
    private Boolean useDifficultyTiers;
    private Double totalExamPoint;
    private Integer questionQuota;
    private Double questionQuotaPoint;
    private Integer easyQuota;
    private Double easyQuotaPoint;
    private Integer mediumQuota;
    private Double mediumQuotaPoint;
    private Integer hardQuota;
    private Double hardQuotaPoint;
    private Integer maxQuestionChanges;
    private Integer maxCheatEvents;
    private CheatBlockMode cheatBlockMode;

    // languages
    private List<ProgrammingLanguageResponse> programmingLanguages;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}