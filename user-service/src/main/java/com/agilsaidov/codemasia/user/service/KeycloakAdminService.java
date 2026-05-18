package com.agilsaidov.codemasia.user.service;

import com.agilsaidov.codemasia.user.config.KeycloakProperties;
import com.agilsaidov.codemasia.user.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final KeycloakProperties keycloakProperties;
    private final WebClient.Builder webClientBuilder;

    private String getAdminToken() {
        WebClient webClient = webClientBuilder.build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("client_secret", keycloakProperties.getClientSecret());

        return webClient.post()
                .uri(keycloakProperties.getServerUrl() +
                        "/realms/" + keycloakProperties.getRealm() +
                        "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .block();
    }

    public void changePassword(UUID keycloakId, String newPassword) {
        String token = getAdminToken();
        WebClient webClient = webClientBuilder.build();

        Map<String, Object> passwordBody = new HashMap<>();
        passwordBody.put("type", "password");
        passwordBody.put("value", newPassword);
        passwordBody.put("temporary", false);

        webClient
                .put()
                .uri(keycloakProperties.getServerUrl() +
                        "/admin/realms/" + keycloakProperties.getRealm() +
                        "/users/" + keycloakId + "/reset-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passwordBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).map(body ->
                                        new BadRequestException("KEYCLOAK_ERROR", body)))
                .toBodilessEntity()
                .block();

        //Delete all previous Sessions / Refresh tokens
        webClient.post()
                .uri(keycloakProperties.getServerUrl()
                        + "/admin/realms/" + keycloakProperties.getRealm()
                        + "/users/" + keycloakId + "/logout")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).map(body ->
                                new BadRequestException("KEYCLOAK_ERROR", body)))
                .toBodilessEntity()
                .block();
        log.info("Password changed for keycloakId={}", keycloakId);
    }
}