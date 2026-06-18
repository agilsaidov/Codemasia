package com.agilsaidov.codemasia.exam.client;

import com.agilsaidov.codemasia.exam.dto.clientdto.response.GroupDetails;
import com.agilsaidov.codemasia.exam.exception.BadRequestException;
import com.agilsaidov.codemasia.exam.exception.ForbiddenException;
import com.agilsaidov.codemasia.exam.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
public class GroupClient {

    private final WebClient webClient;

    public GroupClient(WebClient.Builder webClientBuilder,
                       @Value("${app.user-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public GroupDetails getGroupById(String groupId, UUID userId, String role) {
        log.debug("Fetching group {} from user-service for user {}", groupId, userId);

        try {
            return webClient.get()
                    .uri("api/groups/{groupId}", groupId)
                    .header("X-User-Id", userId.toString())
                    .header("X-User-Role", role)
                    .retrieve()
                    .onStatus(
                            status -> status.isSameCodeAs(HttpStatus.NOT_FOUND),
                            response -> Mono.error(new NotFoundException(
                                    "GROUP_NOT_FOUND",
                                    "Group with id " + groupId + " not found"))
                    )
                    .onStatus(
                            status -> status.isSameCodeAs(HttpStatus.FORBIDDEN),
                            response -> Mono.error(new ForbiddenException(
                                    "FORBIDDEN_ACTION",
                                    "You are not allowed to access this group"))
                    )
                    .bodyToMono(GroupDetails.class)
                    .block(Duration.ofSeconds(5));

        } catch (NotFoundException | ForbiddenException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("Failed to fetch group {}: status={} body={}", groupId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("USER_SERVICE_UNAVAILABLE", "Could not verify group");
        }
    }
}
