package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.DatosCliente;
import java.util.Collection;
import java.util.List;

public interface DatosClienteGateway {
    /** Datos de los solicitantes que existan para los documentos indicados; omite los desconocidos. */
    List<DatosCliente> buscarPorDocumentos(Collection<String> documentos);
}
