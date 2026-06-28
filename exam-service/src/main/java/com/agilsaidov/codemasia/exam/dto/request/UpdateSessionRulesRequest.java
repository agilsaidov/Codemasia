package com.agilsaidov.codemasia.exam.dto.request;

import com.agilsaidov.codemasia.exam.model.CheatBlockMode;
import com.agilsaidov.codemasia.exam.model.SelectionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateSessionRulesRequest {

    @NotNull(message = "Field 'selectionMode' is required")
    private SelectionMode selectionMode;

    @NotNull(message = "Field 'useDifficultyTiers' is required")
    private Boolean useDifficultyTiers;

    @NotNull(message = "Field 'questionQuota' is required")
    @Min(value = 0, message = "Field 'questionQuota' cannot be negative")
    private Integer questionQuota;

    @NotNull(message = "Field 'questionQuotaPoint' is required")
    @Min(value = 0, message = "Field 'questionQuotaPoint' cannot be negative")
    private Double questionQuotaPoint;

    @NotNull(message = "Field 'easyQuota' is required")
    @Min(value = 0, message = "Field 'easyQuota' cannot be negative")
    private Integer easyQuota;

    @NotNull(message = "Field 'easyQuotaPoint' is required")
    @Min(value = 0, message = "Field 'easyQuotaPoint' cannot be negative")
    private Double easyQuotaPoint;

    @NotNull(message = "Field 'mediumQuota' is required")
    @Min(value = 0, message = "Field 'mediumQuota' cannot be negative")
    private Integer mediumQuota;

    @NotNull(message = "Field 'mediumQuotaPoint' is required")
    @Min(value = 0, message = "Field 'mediumQuotaPoint' cannot be negative")
    private Double mediumQuotaPoint;

    @NotNull(message = "Field 'hardQuota' is required")
    @Min(value = 0, message = "Field 'hardQuota' cannot be negative")
    private Integer hardQuota;

    @NotNull(message = "Field 'hardQuotaPoint' is required")
    @Min(value = 0, message = "Field 'hardQuotaPoint' cannot be negative")
    private Double hardQuotaPoint;

    @NotNull(message = "Field 'totalExamPoint' is required")
    @Min(value = 0, message = "Field 'totalExamPoint' cannot be negative")
    private Double totalExamPoint;

    @NotNull(message = "Field 'maxQuestionChanges' is required")
    @Min(value = 0, message = "Field 'maxQuestionChanges' cannot be negative")
    private Integer maxQuestionChanges;

    @NotNull(message = "Field 'maxCheatEvents' is required")
    @Min(value = 0, message = "Field 'maxCheatEvents' cannot be negative")
    private Integer maxCheatEvents;

    @NotNull(message = "Field 'cheatBlockMode' is required")
    private CheatBlockMode cheatBlockMode;
}
