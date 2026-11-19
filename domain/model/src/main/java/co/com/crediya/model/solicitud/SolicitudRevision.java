package co.com.crediya.model.solicitud;

import java.math.BigDecimal;

/**
 * Una solicitud con los datos que un asesor necesita para decidir sobre ella.
 *
 * @param cliente datos del solicitante, o {@code null} si el usuario no existe
 * @param deudaMensualAprobadas suma de las cuotas mensuales de las solicitudes aprobadas del solicitante
 */
public record SolicitudRevision(
        Solicitud solicitud, DatosCliente cliente, BigDecimal deudaMensualAprobadas) {
}
