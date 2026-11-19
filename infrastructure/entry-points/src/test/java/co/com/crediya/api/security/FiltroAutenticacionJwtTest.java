package co.com.crediya.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.model.autenticacion.Rol;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.autenticacion.exception.VerificacionTokensNoDisponibleException;
import co.com.crediya.model.autenticacion.gateways.VerificadorTokens;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

class FiltroAutenticacionJwtTest {
    private static final UsuarioAutenticado USUARIO =
            new UsuarioAutenticado(UUID.randomUUID(), "ana@example.com", Rol.ASESOR, null);

    private final VerificadorTokens verificador = token -> switch (token) {
        case "valido" -> USUARIO;
        case "sin-claves" -> throw new VerificacionTokensNoDisponibleException(
                "JWKS no disponible", new IllegalStateException());
        default -> throw new TokenInvalidoException("Token inválido.");
    };
    private final FiltroAutenticacionJwt filtro = new FiltroAutenticacionJwt(verificador);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWithValidToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        run("Bearer valido", chain, new MockHttpServletResponse());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(chain.getRequest());
        assertEquals(USUARIO, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ASESOR".equals(authority.getAuthority())));
    }

    @Test
    void leavesRequestAnonymousWhenTokenIsInvalid() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        run("Bearer falso", chain, new MockHttpServletResponse());

        assertNotNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void leavesRequestAnonymousWithoutBearerHeader() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        run(null, chain, new MockHttpServletResponse());
        run("Basic abc", new MockFilterChain(), new MockHttpServletResponse());

        assertNotNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void answersServiceUnavailableWhenKeysCannotBeFetched() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        run("Bearer sin-claves", chain, response);

        assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(503, response.getStatus());
        assertEquals("AUTENTICACION_NO_DISPONIBLE", JsonMapper.builder().build()
                .readTree(response.getContentAsString()).get("codigo").asString());
    }

    private void run(String authorization, MockFilterChain chain, MockHttpServletResponse response)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        filtro.doFilter(request, response, chain);
    }
}
