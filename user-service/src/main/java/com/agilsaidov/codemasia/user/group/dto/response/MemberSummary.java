package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class MemberSummary {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private OffsetDateTime joinedAt;
}
