package co.com.crediya.api;

import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.usecase.solicitud.exception.SolicitudValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(SolicitudValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(SolicitudValidationException exception) {
        LOGGER.warn("Validación fallida al registrar solicitud: {}", exception.getMessage());
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", exception.getMessage());
    }

    @ExceptionHandler(TipoPrestamoNoExisteException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownLoanType(
            TipoPrestamoNoExisteException exception) {
        LOGGER.warn("Solicitud con tipo de préstamo inexistente.");
        return error(HttpStatus.BAD_REQUEST, "TIPO_PRESTAMO_INVALIDO", exception.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AccesoDenegadoException exception) {
        LOGGER.warn("Solicitud rechazada: el cliente intentó actuar en nombre de otra persona.");
        return error(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", exception.getMessage());
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownClient(
            ClienteNoEncontradoException exception) {
        LOGGER.warn("Solicitud con documento de cliente inexistente.");
        return error(HttpStatus.NOT_FOUND, "CLIENTE_NO_ENCONTRADO", exception.getMessage());
    }

    @ExceptionHandler(ClienteServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleClientServiceUnavailable(
            ClienteServiceUnavailableException exception) {
        LOGGER.error("Servicio de usuarios no disponible.", exception);
        return error(
                HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(
            MethodArgumentTypeMismatchException exception) {
        LOGGER.warn("Parámetro de consulta inválido: {}", exception.getName());
        return error(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_INVALIDO",
                "El valor del parámetro '" + exception.getName() + "' no es válido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception) {
        LOGGER.warn("Cuerpo de la solicitud con formato inválido.");
        return error(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud no tiene un formato válido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return handleFrameworkError(errorResponse);
        }
        LOGGER.error("Error inesperado al procesar la solicitud.", exception);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrió un error interno. Intente nuevamente más tarde.");
    }

    private ResponseEntity<ApiErrorResponse> handleFrameworkError(ErrorResponse errorResponse) {
        HttpStatusCode status = errorResponse.getStatusCode();
        LOGGER.warn("Solicitud HTTP no soportada: {}", status);
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        "SOLICITUD_NO_SOPORTADA",
                        "La solicitud no puede ser procesada por este servicio."));
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), code, message));
    }
}
