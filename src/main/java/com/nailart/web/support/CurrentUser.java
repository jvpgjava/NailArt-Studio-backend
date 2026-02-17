package com.nailart.web.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

public class CurrentUser {

    public static Optional<String> getKeycloakId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        String sub = jwt.getSubject();
        return Optional.ofNullable(sub);
    }

    public static String getKeycloakIdOrThrow() {
        return getKeycloakId().orElseThrow(() -> new IllegalStateException("Usuário não autenticado"));
    }
}