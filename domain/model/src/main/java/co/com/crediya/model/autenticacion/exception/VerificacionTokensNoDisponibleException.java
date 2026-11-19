package co.com.crediya.model.autenticacion.exception;

/**
 * No se pudo comprobar un token porque las claves de verificación no están disponibles
 * (por ejemplo, el servicio de autenticación no responde). No dice nada sobre el token.
 */
public class VerificacionTokensNoDisponibleException extends RuntimeException {
    public VerificacionTokensNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
