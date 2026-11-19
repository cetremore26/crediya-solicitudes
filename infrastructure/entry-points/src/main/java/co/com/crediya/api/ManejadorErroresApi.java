package co.com.crediya.api;

import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.ServicioUsuariosRechazoException;
import co.com.crediya.model.solicitud.exception.ServicioUsuariosNoDisponibleException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
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
public class ManejadorErroresApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManejadorErroresApi.class);

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorApiResponse> manejarValidacion(SolicitudInvalidaException exception) {
        LOGGER.warn("Validación fallida: {}", exception.getMessage());
        return respuestaError(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", exception.getMessage());
    }

    @ExceptionHandler(TipoPrestamoNoExisteException.class)
    public ResponseEntity<ErrorApiResponse> manejarTipoPrestamoInexistente(
            TipoPrestamoNoExisteException exception) {
        LOGGER.warn("Solicitud con tipo de préstamo inexistente.");
        return respuestaError(HttpStatus.BAD_REQUEST, "TIPO_PRESTAMO_INVALIDO", exception.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorApiResponse> manejarAccesoDenegado(AccesoDenegadoException exception) {
        LOGGER.warn("Operación rechazada: el usuario no tiene permiso.");
        return respuestaError(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", exception.getMessage());
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ErrorApiResponse> manejarClienteNoEncontrado(
            ClienteNoEncontradoException exception) {
        LOGGER.warn("Solicitud con documento de cliente inexistente.");
        return respuestaError(HttpStatus.UNPROCESSABLE_CONTENT, "CLIENTE_NO_ENCONTRADO", exception.getMessage());
    }

    @ExceptionHandler(ServicioUsuariosNoDisponibleException.class)
    public ResponseEntity<ErrorApiResponse> manejarServicioUsuariosNoDisponible(
            ServicioUsuariosNoDisponibleException exception) {
        LOGGER.error("Servicio de usuarios no disponible.", exception);
        return respuestaError(
                HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE", exception.getMessage());
    }

    @ExceptionHandler(ServicioUsuariosRechazoException.class)
    public ResponseEntity<ErrorApiResponse> manejarRechazoServicioUsuarios(
            ServicioUsuariosRechazoException exception) {
        LOGGER.error("El servicio de usuarios rechazó la consulta: {}", exception.getMessage());
        return respuestaError(HttpStatus.BAD_GATEWAY, "INTEGRACION_USUARIOS_FALLIDA",
                "No fue posible consultar los datos del cliente. Intente más tarde.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApiResponse> manejarParametroInvalido(
            MethodArgumentTypeMismatchException exception) {
        LOGGER.warn("Parámetro de consulta inválido: {}", exception.getName());
        return respuestaError(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_INVALIDO",
                "El valor del parámetro '" + exception.getName() + "' no es válido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApiResponse> manejarCuerpoInvalido(
            HttpMessageNotReadableException exception) {
        LOGGER.warn("Cuerpo de la solicitud con formato inválido.");
        return respuestaError(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud no tiene un formato válido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> manejarInesperado(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return manejarErrorDelFramework(errorResponse);
        }
        LOGGER.error("Error inesperado al procesar la solicitud.", exception);
        return respuestaError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR_INTERNO",
                "Ocurrió un error interno. Intente nuevamente más tarde.");
    }

    private ResponseEntity<ErrorApiResponse> manejarErrorDelFramework(ErrorResponse errorResponse) {
        HttpStatusCode status = errorResponse.getStatusCode();
        LOGGER.warn("Solicitud HTTP no soportada: {}", status);
        return ResponseEntity.status(status)
                .body(new ErrorApiResponse(
                        status.value(),
                        "SOLICITUD_NO_SOPORTADA",
                        "La solicitud no puede ser procesada por este servicio."));
    }

    private ResponseEntity<ErrorApiResponse> respuestaError(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorApiResponse(status.value(), code, message));
    }
}
