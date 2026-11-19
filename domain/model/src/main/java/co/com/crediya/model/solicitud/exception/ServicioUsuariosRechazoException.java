package co.com.crediya.model.solicitud.exception;

/**
 * El servicio de usuarios respondió, pero rechazó la consulta (por ejemplo por token o
 * permisos). A diferencia de {@link ServicioUsuariosNoDisponibleException}, reintentar no ayuda:
 * es un problema de integración entre servicios.
 */
public class ServicioUsuariosRechazoException extends RuntimeException {
    public ServicioUsuariosRechazoException(int status) {
        super("El servicio de usuarios rechazó la consulta (HTTP " + status + ").");
    }
}
