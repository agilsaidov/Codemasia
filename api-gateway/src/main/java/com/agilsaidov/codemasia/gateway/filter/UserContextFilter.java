package com.agilsaidov.codemasia.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserContextFilter implements  GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    Jwt jwt = auth.getToken();
                    String userId =  jwt.getSubject();
                    String role = extractRole(jwt);

                    log.debug("Routing request for userId={} role={} path={}",
                            userId, role, exchange.getRequest().getPath());

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", role)
                            .header("X-User-Email", jwt.getClaimAsString("email"))
                            .build();

                    return chain.filter(
                            exchange.mutate().request(mutatedRequest).build()
                    );
                })
                .doOnError(e -> log.error("Failed to process request: {}", e.getMessage()));
    }

    private String extractRole(Jwt jwt){
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if(realmAccess.isEmpty()){
            log.warn("Token without realm_access for subject {}", jwt.getSubject());
            throw new RuntimeException("Missing realm_access claim in token");
        }

        List<String> roles = (List<String>) realmAccess.get("roles");

        if(roles.isEmpty()){
            log.warn("Token has no roles for subject={}", jwt.getSubject());
            throw new IllegalArgumentException("No roles found in token");
        }

        return roles.stream()
                .filter(role -> role.equals("ADMIN") || role.equals("TEACHER") || role.equals("STUDENT"))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No valid role found for subject {}", jwt.getSubject());
                    return new IllegalArgumentException("No valid role in token");
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
