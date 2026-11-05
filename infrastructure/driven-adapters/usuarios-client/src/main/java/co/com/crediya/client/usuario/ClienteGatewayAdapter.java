package co.com.crediya.client.usuario;

import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import co.com.crediya.model.solicitud.gateways.ClienteGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ClienteGatewayAdapter implements ClienteGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteGatewayAdapter.class);
    private static final String USUARIO_POR_DOCUMENTO_PATH = "/api/v1/usuarios/documento/{documento}";

    private final RestClient usuariosRestClient;

    public ClienteGatewayAdapter(RestClient usuariosRestClient) {
        this.usuariosRestClient = usuariosRestClient;
    }

    @Override
    public boolean existsByDocumentoIdentidad(String documentoIdentidad) {
        try {
            return usuariosRestClient.get()
                    .uri(USUARIO_POR_DOCUMENTO_PATH, documentoIdentidad)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                            return false;
                        }
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return true;
                        }
                        throw new IllegalStateException(
                                "Respuesta inesperada del servicio de usuarios: "
                                        + response.getStatusCode());
                    });
        } catch (RestClientException | IllegalStateException exception) {
            LOGGER.error("Fallo al consultar el servicio de usuarios.", exception);
            throw new ClienteServiceUnavailableException(exception);
        }
    }
}
