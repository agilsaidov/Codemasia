package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.dto.request.CreateUserRequest;
import com.agilsaidov.codemasia.user.dto.request.UpdateUserRequest;
import com.agilsaidov.codemasia.user.exception.DuplicateException;
import com.agilsaidov.codemasia.user.model.Role;
import jakarta.ws.rs.WebApplicationException;
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
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final UsersResource usersResource;
    private final RealmResource realmResource;


    public UUID createUser(CreateUserRequest request) {
        log.debug("Creating Keycloak user username={}", request.getUsername());

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
        log.debug("Keycloak user created keycloakId={}", keycloakId);
        return UUID.fromString(keycloakId);
    }


    public void deleteUser(UUID keycloakId) {
        log.debug("Deleting Keycloak user keycloakId={}", keycloakId);
        usersResource.get(keycloakId.toString()).remove();
        log.debug("Keycloak user deleted keycloakId={}", keycloakId);
    }


    public void enableUser(UUID keycloakId, Boolean enable) {
        log.debug("Setting enabled={} for keycloakId={}", enable, keycloakId);
        UserResource userResource = usersResource.get(keycloakId.toString());
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(enable);
        userResource.update(user);
        log.debug("Keycloak enable status updated keycloakId={}", keycloakId);
    }


    public void changeEmail(UUID keycloakId, String email) {
        log.debug("Changing email for keycloakId={}", keycloakId);
        updateUser(keycloakId, user -> user.setEmail(email));
        log.debug("Email changed in Keycloak keycloakId={}", keycloakId);
    }


    public void changePassword(UUID keycloakId, String password) {
        log.debug("Changing password for keycloakId={}", keycloakId);
        CredentialRepresentation credential = createCredential(password);
        usersResource.get(keycloakId.toString()).resetPassword(credential);
        log.debug("Password changed in Keycloak keycloakId={}", keycloakId);
    }


    public void assignRole(UUID keycloakId, Role role) {
        log.debug("Assigning role={} to keycloakId={}", role.name(), keycloakId);
        try {

            RoleRepresentation newRoleRepresentation = realmResource.roles()
                    .get(role.name()).toRepresentation();

            var realmRoles = usersResource.get(keycloakId.toString())
                    .roles()
                    .realmLevel();

            List<RoleRepresentation> current = realmRoles.listAll();

            current.stream()
                    .filter(r -> List.of("ADMIN", "TEACHER", "STUDENT").contains(r.getName()))
                    .forEach(r -> realmRoles.remove(List.of(r)));

            realmRoles.add(List.of(newRoleRepresentation));

            log.debug("Role={} assigned in Keycloak keycloakId={}", role.name(), keycloakId);
        } catch (Exception e) {
            log.error("Failed to assign role={} to keycloakId={}: {}", role.name(), keycloakId, e.getMessage());
            throw e;
        }
    }


    private void updateUser(UUID keycloakId, Consumer<UserRepresentation> updater) {
        UserResource userResource = usersResource.get(keycloakId.toString());
        UserRepresentation user = userResource.toRepresentation();
        updater.accept(user);
        
        try {
            userResource.update(user);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 409) {
                log.warn("User already exists in Keycloak: keycloakId={}", keycloakId);
                throw new DuplicateException("USER_ALREADY_EXISTS",
                        "Username or email already taken by another user");
            }
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

