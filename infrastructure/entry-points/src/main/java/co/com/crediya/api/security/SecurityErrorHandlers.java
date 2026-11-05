package co.com.crediya.api.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

public final class SecurityErrorHandlers {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityErrorHandlers.class);
    private static final String BODY_TEMPLATE =
            "{\"status\":%d,\"codigo\":\"%s\",\"mensaje\":\"%s\"}";

    private SecurityErrorHandlers() {
    }

    public static AuthenticationEntryPoint unauthorized() {
        return (request, response, exception) -> {
            LOGGER.warn("Acceso sin autenticación válida a {}.", request.getRequestURI());
            write(response, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                    "Debe iniciar sesión para acceder a este recurso.");
        };
    }

    public static AccessDeniedHandler forbidden() {
        return (request, response, exception) -> {
            LOGGER.warn("Acceso denegado por rol a {}.", request.getRequestURI());
            write(response, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "No tiene permisos para realizar esta acción.");
        };
    }

    private static void write(
            HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(String.format(BODY_TEMPLATE, status.value(), code, message));
    }
}
