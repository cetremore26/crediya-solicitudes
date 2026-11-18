package co.com.crediya.client.usuario;

import co.com.crediya.model.solicitud.exception.ClienteServiceRejectedException;
import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import org.springframework.http.HttpStatusCode;

/** Traduce las respuestas de error del servicio de usuarios a excepciones del dominio. */
final class UsuariosServiceErrors {
    private UsuariosServiceErrors() {
    }

    /**
     * 4xx: el servicio rechazó la consulta (integración); 5xx u otros: no está disponible.
     */
    static RuntimeException fromStatus(HttpStatusCode status) {
        if (status.is4xxClientError()) {
            return new ClienteServiceRejectedException(status.value());
        }
        return new ClienteServiceUnavailableException(
                new IllegalStateException("Respuesta del servicio de usuarios: " + status));
    }
}
