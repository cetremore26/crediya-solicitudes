package co.com.crediya.client.usuario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ClienteGatewayAdapterTest {
    private static final String URL = "http://usuarios/api/v1/usuarios/documento/1020304050";

    private MockRestServiceServer server;
    private ClienteGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://usuarios");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new ClienteGatewayAdapter(builder.build());
    }

    @Test
    void returnsTrueWhenClientExists() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertTrue(adapter.existsByDocumentoIdentidad("1020304050"));
    }

    @Test
    void returnsFalseWhenClientDoesNotExist() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertFalse(adapter.existsByDocumentoIdentidad("1020304050"));
    }

    @Test
    void throwsUnavailableWhenUsersServiceFails() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThrows(
                ClienteServiceUnavailableException.class,
                () -> adapter.existsByDocumentoIdentidad("1020304050"));
    }
}
