package co.com.crediya.model.solicitud;

import java.util.Set;

/** Filter and paging options to list applications that need a manual decision. */
public record FiltroRevision(
        Set<EstadoSolicitud> estados, String tipoPrestamo, int page, int size) {
}
