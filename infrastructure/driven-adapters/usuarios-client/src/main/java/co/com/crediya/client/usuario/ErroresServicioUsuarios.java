package co.com.crediya.client.usuario;

import co.com.crediya.model.solicitud.exception.ServicioUsuariosRechazoException;
import co.com.crediya.model.solicitud.exception.ServicioUsuariosNoDisponibleException;
import org.springframework.http.HttpStatusCode;

/** Traduce las respuestas de error del servicio de usuarios a excepciones del dominio. */
final class ErroresServicioUsuarios {
    private ErroresServicioUsuarios() {
    }

    /**
     * 4xx: el servicio rechazó la consulta (integración); 5xx u otros: no está disponible.
     */
    static RuntimeException desdeEstado(HttpStatusCode status) {
        if (status.is4xxClientError()) {
            return new ServicioUsuariosRechazoException(status.value());
        }
        return new ServicioUsuariosNoDisponibleException(
                new IllegalStateException("Respuesta del servicio de usuarios: " + status));
    }
}
