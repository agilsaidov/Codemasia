package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter
public class MemberSummary {
    private UUID id;
    private String email;
    private String name;
    private String surname;
    private OffsetDateTime joinedAt;
    private Boolean enabled;
}
