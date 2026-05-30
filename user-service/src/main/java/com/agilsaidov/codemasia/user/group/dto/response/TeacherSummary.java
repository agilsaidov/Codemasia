package com.agilsaidov.codemasia.user.group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class TeacherSummary {
    private UUID id;
    private String name;
    private String surname;
}