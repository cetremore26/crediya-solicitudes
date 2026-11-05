package co.com.crediya.model.solicitud;

import java.math.BigDecimal;

/**
 * An application with the data an advisor needs to decide on it.
 *
 * @param cliente applicant data, or {@code null} when the user is not found
 * @param deudaMensualAprobadas sum of the monthly payments of the applicant's approved loans
 */
public record SolicitudRevision(
        Solicitud solicitud, DatosCliente cliente, BigDecimal deudaMensualAprobadas) {
}
