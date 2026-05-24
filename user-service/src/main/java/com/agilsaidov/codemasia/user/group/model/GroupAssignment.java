package com.agilsaidov.codemasia.user.group.model;

import com.agilsaidov.codemasia.user.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@Table(name = "group_assignments")
public class GroupAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "ends_at")
    private OffsetDateTime endsAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    public void onCreate() {
        assignedAt = OffsetDateTime.now();
        if (active == null) {
            active = true;
        }
    }
}
