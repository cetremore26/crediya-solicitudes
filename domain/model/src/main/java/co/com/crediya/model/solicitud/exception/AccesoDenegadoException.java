package co.com.crediya.model.solicitud.exception;

public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException() {
        super("Solo puede registrar solicitudes de préstamo a su nombre.");
    }
}
