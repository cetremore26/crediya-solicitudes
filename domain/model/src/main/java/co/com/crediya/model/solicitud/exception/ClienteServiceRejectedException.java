package co.com.crediya.model.solicitud.exception;

/**
 * El servicio de usuarios respondió, pero rechazó la consulta (por ejemplo por token o
 * permisos). A diferencia de {@link ClienteServiceUnavailableException}, reintentar no ayuda:
 * es un problema de integración entre servicios.
 */
public class ClienteServiceRejectedException extends RuntimeException {
    public ClienteServiceRejectedException(int status) {
        super("El servicio de usuarios rechazó la consulta (HTTP " + status + ").");
    }
}
