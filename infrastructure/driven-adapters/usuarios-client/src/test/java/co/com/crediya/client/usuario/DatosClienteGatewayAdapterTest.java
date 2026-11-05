package co.com.crediya.client.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DatosClienteGatewayAdapterTest {
    private static final String URL = "http://usuarios/api/v1/usuarios?documentos=111111,222222";

    private MockRestServiceServer server;
    private DatosClienteGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://usuarios");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new DatosClienteGatewayAdapter(builder.build());
    }

    @Test
    void mapsUsersServicePayloadToClientData() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                [{"documento_identidad": "111111", "nombres": "Ana", "apellidos": "Pérez",
                  "correo_electronico": "ana@example.com", "salario_base": 2500000,
                  "campo_nuevo": "ignorado"}]
                """, MediaType.APPLICATION_JSON));

        List<DatosCliente> clientes = adapter.findByDocumentos(List.of("111111", "222222"));

        assertEquals(1, clientes.size());
        assertEquals("ana@example.com", clientes.get(0).correoElectronico());
        assertEquals(0, clientes.get(0).salarioBase().compareTo(new java.math.BigDecimal("2500000")));
    }

    @Test
    void throwsUnavailableWhenUsersServiceFails() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThrows(ClienteServiceUnavailableException.class,
                () -> adapter.findByDocumentos(List.of("111111", "222222")));
    }
}
