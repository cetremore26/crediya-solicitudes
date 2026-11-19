package co.com.crediya.api.security;

import co.com.crediya.api.ErrorApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

/**
 * Respuestas JSON de seguridad (401/403/503) con el mismo formato que {@link ErrorApiResponse}.
 * Se serializan con Jackson para que el cuerpo siempre sea JSON válido.
 */
public final class RespuestasErrorSeguridad {
    private static final Logger LOGGER = LoggerFactory.getLogger(RespuestasErrorSeguridad.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private RespuestasErrorSeguridad() {
    }

    public static AuthenticationEntryPoint noAutenticado() {
        return (request, response, exception) -> {
            LOGGER.warn("Acceso sin autenticación válida a {}.", request.getRequestURI());
            escribir(response, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                    "Debe iniciar sesión para acceder a este recurso.");
        };
    }

    public static AccessDeniedHandler accesoDenegado() {
        return (request, response, exception) -> {
            LOGGER.warn("Acceso denegado por rol a {}.", request.getRequestURI());
            escribir(response, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "No tiene permisos para realizar esta acción.");
        };
    }

    /** El token no se pudo comprobar porque las claves de verificación no están disponibles. */
    public static void verificacionNoDisponible(HttpServletResponse response) throws IOException {
        escribir(response, HttpStatus.SERVICE_UNAVAILABLE, "AUTENTICACION_NO_DISPONIBLE",
                "No se puede verificar la sesión en este momento. Intente más tarde.");
    }

    private static void escribir(
            HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                JSON.writeValueAsString(new ErrorApiResponse(status.value(), code, message)));
    }
}
