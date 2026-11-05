package co.com.crediya.model.solicitud.exception;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException() {
        super("No existe un cliente registrado con ese documento de identidad.");
    }
}
