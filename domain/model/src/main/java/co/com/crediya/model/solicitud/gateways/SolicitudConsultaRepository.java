package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import java.util.Collection;
import java.util.List;

public interface SolicitudConsultaRepository {
    /** Applications in the requested states, oldest first, with the loan type interest rate. */
    PageResult<Solicitud> findForReview(FiltroRevision filtro);

    /** Approved applications of the given applicants, with the loan type interest rate. */
    List<Solicitud> findApprovedByDocumentos(Collection<String> documentos);
}
