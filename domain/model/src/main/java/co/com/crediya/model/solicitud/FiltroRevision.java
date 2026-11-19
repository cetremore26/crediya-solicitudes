package co.com.crediya.model.solicitud;

import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Filtro y paginación para listar las solicitudes que necesitan una decisión manual.
 * Siempre es válido: el constructor rechaza paginación o estados no permitidos, normaliza el
 * tipo de préstamo y guarda una copia inmutable de los estados.
 */
public record FiltroRevision(
        Set<EstadoSolicitud> estados, String tipoPrestamo, int pagina, int tamano) {
    public static final int TAMANO_MAXIMO_PAGINA = 50;
    private static final Set<EstadoSolicitud> ESTADOS_REVISABLES = Set.copyOf(
            Arrays.stream(EstadoSolicitud.values()).filter(EstadoSolicitud::esRevisable).toList());

    /**
     * @throws SolicitudInvalidaException si la paginación o los estados no están permitidos
     */
    public FiltroRevision {
        if (pagina < 0) {
            throw new SolicitudInvalidaException("La página no puede ser negativa.");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new SolicitudInvalidaException(
                    "El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA + ".");
        }
        if ((long) pagina * tamano > Integer.MAX_VALUE) {
            throw new SolicitudInvalidaException("La página solicitada está fuera de rango.");
        }
        if (estados == null || estados.isEmpty() || !ESTADOS_REVISABLES.containsAll(estados)) {
            throw new SolicitudInvalidaException(
                    "Solo se pueden listar solicitudes pendientes de revisión, rechazadas o en revisión manual.");
        }
        estados = Set.copyOf(estados);
        tipoPrestamo = Solicitud.normalizarTipoPrestamo(tipoPrestamo);
    }

    /**
     * Construye un filtro desde la petición: sin estados usa todos los estados revisables.
     *
     * @throws SolicitudInvalidaException si la paginación o los estados no están permitidos
     */
    public static FiltroRevision de(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, int pagina, int tamano) {
        Set<EstadoSolicitud> estadosPedidos = estados == null || estados.isEmpty()
                ? ESTADOS_REVISABLES
                : Set.copyOf(estados);
        return new FiltroRevision(estadosPedidos, tipoPrestamo, pagina, tamano);
    }
}
