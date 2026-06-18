package com.agilsaidov.codemasia.exam.dto.clientdto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class GroupDetails {
    private String groupId;
    private String name;
    private Boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
