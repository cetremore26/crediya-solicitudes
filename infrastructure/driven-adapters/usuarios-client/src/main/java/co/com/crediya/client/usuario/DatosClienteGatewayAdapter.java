package co.com.crediya.client.usuario;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import co.com.crediya.model.solicitud.gateways.DatosClienteGateway;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class DatosClienteGatewayAdapter implements DatosClienteGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatosClienteGatewayAdapter.class);
    private static final String USUARIOS_PATH = "/api/v1/usuarios";

    private final RestClient usuariosRestClient;

    public DatosClienteGatewayAdapter(RestClient usuariosRestClient) {
        this.usuariosRestClient = usuariosRestClient;
    }

    @Override
    public List<DatosCliente> findByDocumentos(Collection<String> documentos) {
        try {
            List<UsuarioPayload> usuarios = usuariosRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(USUARIOS_PATH)
                            .queryParam("documentos", String.join(",", documentos))
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, currentAuthorization())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UsuarioPayload>>() { });
            return usuarios == null
                    ? List.of()
                    : usuarios.stream().map(UsuarioPayload::toDomain).toList();
        } catch (RestClientException exception) {
            LOGGER.error("Fallo al consultar los datos de clientes en el servicio de usuarios.", exception);
            throw new ClienteServiceUnavailableException(exception);
        }
    }

    private String currentAuthorization() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                return authorization;
            }
        }
        return "";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UsuarioPayload(
            @JsonProperty("documento_identidad") String documentoIdentidad,
            String nombres,
            String apellidos,
            @JsonProperty("correo_electronico") String correoElectronico,
            @JsonProperty("salario_base") BigDecimal salarioBase) {

        DatosCliente toDomain() {
            return new DatosCliente(
                    documentoIdentidad, nombres, apellidos, correoElectronico, salarioBase);
        }
    }
}
