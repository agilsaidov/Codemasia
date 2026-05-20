package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.dto.request.CreateUserRequest;
import com.agilsaidov.codemasia.user.dto.request.UpdateUserRequest;
import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.model.Role;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final UsersResource usersResource;
    private final RealmResource realmResource;

    public UUID createUser(CreateUserRequest request) {
        log.info("Creating Keycloak user with username={}", request.getUsername());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(List.of(createCredential(request.getPassword())));

        Response response = usersResource.create(user);

        if (response.getStatus() == 409) {
            log.warn("User already exists in Keycloak: username={}", request.getUsername());
            throw new DuplicateException("USER_ALREADY_EXISTS",
                    "User with this username already exists");
        }
        if (response.getStatus() != 201) {
            log.error("Failed to create Keycloak user: status={}", response.getStatus());
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
        }

        String locationPath = response.getLocation().getPath();
        String keycloakId = locationPath.substring(locationPath.lastIndexOf('/') + 1);
        log.info("Keycloak user created successfully keycloakId={}", keycloakId);
        return UUID.fromString(keycloakId);
    }

    public void deleteUser(UUID keycloakId) {
        log.info("Deleting Keycloak user keycloakId={}", keycloakId);
        usersResource.get(keycloakId.toString()).remove();
        log.info("Keycloak user deleted keycloakId={}", keycloakId);
    }

    public void enableUser(UUID keycloakId, Boolean enable) {
        log.info("Setting enabled={} for keycloakId={}", enable, keycloakId);
        UserResource userResource = usersResource.get(keycloakId.toString());
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enable);
        userResource.update(user);
        log.info("User enable status updated keycloakId={}", keycloakId);
    }

    public void changePassword(UUID keycloakId, String password) {
        log.info("Changing password for keycloakId={}", keycloakId);
        CredentialRepresentation credential = createCredential(password);
        usersResource.get(keycloakId.toString()).resetPassword(credential);
        log.info("Password changed successfully for keycloakId={}", keycloakId);
    }

    public void assignRole(UUID keycloakId, Role role) {
        log.info("Assigning role={} to keycloakId={}", role.name(), keycloakId);
        try {
            RoleRepresentation roleRepresentation = realmResource.roles()
                    .get(role.name())
                    .toRepresentation();
            usersResource.get(keycloakId.toString())
                    .roles()
                    .realmLevel()
                    .add(List.of(roleRepresentation));
            log.info("Role={} assigned to keycloakId={}", role.name(), keycloakId);
        } catch (Exception e) {
            log.error("Failed to assign role={} to keycloakId={}: {}", role.name(), keycloakId, e.getMessage());
            throw e;
        }
    }

    private CredentialRepresentation createCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }
}

