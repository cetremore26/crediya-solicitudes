package co.com.crediya.model.solicitud.exception;

public class TipoPrestamoNoExisteException extends RuntimeException {
    public TipoPrestamoNoExisteException() {
        super("El tipo de préstamo seleccionado no existe.");
    }
}
