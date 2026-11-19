package co.com.crediya.client.usuario;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.exception.ServicioUsuariosNoDisponibleException;
import co.com.crediya.model.solicitud.gateways.DatosClienteGateway;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DatosClienteGatewayAdapter implements DatosClienteGateway {
    private static final String RUTA_USUARIOS = "/api/v1/usuarios";

    private final RestClient usuariosRestClient;

    public DatosClienteGatewayAdapter(RestClient usuariosRestClient) {
        this.usuariosRestClient = usuariosRestClient;
    }

    @Override
    public List<DatosCliente> buscarPorDocumentos(Collection<String> documentos) {
        try {
            List<UsuarioRecibido> usuarios = usuariosRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(RUTA_USUARIOS)
                            .queryParam("documentos", String.join(",", documentos))
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, response) -> {
                        throw ErroresServicioUsuarios.desdeEstado(response.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<List<UsuarioRecibido>>() { });
            return usuarios == null
                    ? List.of()
                    : usuarios.stream().map(UsuarioRecibido::aDominio).toList();
        } catch (RestClientException exception) {
            throw new ServicioUsuariosNoDisponibleException(exception);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UsuarioRecibido(
            @JsonProperty("documento_identidad") String documentoIdentidad,
            String nombres,
            String apellidos,
            @JsonProperty("correo_electronico") String correoElectronico,
            @JsonProperty("salario_base") BigDecimal salarioBase) {

        DatosCliente aDominio() {
            return new DatosCliente(
                    documentoIdentidad, nombres, apellidos, correoElectronico, salarioBase);
        }
    }
}
