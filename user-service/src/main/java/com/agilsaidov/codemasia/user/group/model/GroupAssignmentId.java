package com.agilsaidov.codemasia.user.group.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor @AllArgsConstructor
public class GroupAssignmentId implements Serializable {

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "teacher_id")
    private Long teacherId;
}
