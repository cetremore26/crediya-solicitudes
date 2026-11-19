package co.com.crediya.client.usuario;

import co.com.crediya.model.solicitud.exception.ServicioUsuariosNoDisponibleException;
import co.com.crediya.model.solicitud.gateways.ClienteGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Verifica en el servicio de usuarios si existe un cliente. 404 significa que no existe;
 * los demás errores se traducen con {@link ErroresServicioUsuarios}. El registro del error lo
 * hace quien atiende la excepción, no este adaptador.
 */
@Component
public class ClienteGatewayAdapter implements ClienteGateway {
    private static final String RUTA_USUARIO_POR_DOCUMENTO = "/api/v1/usuarios/documento/{documento}";

    private final RestClient usuariosRestClient;

    public ClienteGatewayAdapter(RestClient usuariosRestClient) {
        this.usuariosRestClient = usuariosRestClient;
    }

    @Override
    public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
        try {
            return usuariosRestClient.get()
                    .uri(RUTA_USUARIO_POR_DOCUMENTO, documentoIdentidad)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                            return false;
                        }
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return true;
                        }
                        throw ErroresServicioUsuarios.desdeEstado(response.getStatusCode());
                    });
        } catch (RestClientException exception) {
            throw new ServicioUsuariosNoDisponibleException(exception);
        }
    }
}
