package co.com.crediya.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class RespuestasErrorSeguridadTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void unauthorizedWritesAValidJsonError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        RespuestasErrorSeguridad.noAutenticado().commence(new MockHttpServletRequest(), response, null);

        JsonNode body = JSON.readTree(response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertEquals(401, body.get("estado").asInt());
        assertEquals("NO_AUTENTICADO", body.get("codigo").asString());
    }

    @Test
    void forbiddenWritesAValidJsonError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        RespuestasErrorSeguridad.accesoDenegado().handle(new MockHttpServletRequest(), response, null);

        JsonNode body = JSON.readTree(response.getContentAsString());
        assertEquals(403, response.getStatus());
        assertEquals("ACCESO_DENEGADO", body.get("codigo").asString());
        assertEquals("No tiene permisos para realizar esta acción.", body.get("mensaje").asString());
    }
}
