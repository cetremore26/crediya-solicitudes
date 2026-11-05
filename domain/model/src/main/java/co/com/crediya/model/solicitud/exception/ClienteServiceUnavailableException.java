package co.com.crediya.model.solicitud.exception;

public class ClienteServiceUnavailableException extends RuntimeException {
    public ClienteServiceUnavailableException(Throwable cause) {
        super("No fue posible verificar al cliente en este momento.", cause);
    }
}
