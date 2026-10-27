package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.DatosCliente;
import java.util.Collection;
import java.util.List;

public interface DatosClienteGateway {
    /** Data of the applicants that exist for the given documents; unknown ones are omitted. */
    List<DatosCliente> findByDocumentos(Collection<String> documentos);
}
