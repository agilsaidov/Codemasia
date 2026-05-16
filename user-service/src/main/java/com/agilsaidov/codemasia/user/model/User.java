package com.agilsaidov.codemasia.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;

    @Column(name = "keycloak_id", nullable = false, unique = true)
    private UUID keycloakId;

    @Column(name = "username",  nullable = false, unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at",  nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at",  nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

}
