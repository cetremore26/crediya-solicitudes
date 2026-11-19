package co.com.crediya.model.solicitud.exception;

public class ServicioUsuariosNoDisponibleException extends RuntimeException {
    public ServicioUsuariosNoDisponibleException(Throwable cause) {
        super("No fue posible verificar al cliente en este momento.", cause);
    }
}
