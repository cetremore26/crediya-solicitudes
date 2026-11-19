package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.Pagina;
import co.com.crediya.model.solicitud.Solicitud;
import java.util.Collection;
import java.util.List;

public interface SolicitudConsultaRepository {
    /** Solicitudes en los estados pedidos, las más antiguas primero, con la tasa de su tipo de préstamo. */
    Pagina<Solicitud> buscarParaRevision(FiltroRevision filtro);

    /** Solicitudes aprobadas de los solicitantes indicados, con la tasa de su tipo de préstamo. */
    List<Solicitud> buscarAprobadasPorDocumentos(Collection<String> documentos);
}
