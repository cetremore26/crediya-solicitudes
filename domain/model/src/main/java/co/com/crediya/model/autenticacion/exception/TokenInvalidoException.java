package co.com.crediya.model.autenticacion.exception;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message) {
        super(message);
    }

    public TokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
