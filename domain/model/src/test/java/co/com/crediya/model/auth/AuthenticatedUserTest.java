package co.com.crediya.model.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticatedUserTest {
    private static final String DOCUMENTO = "1020304050";

    @Test
    void clientActsOnlyForItsOwnDocument() {
        AuthenticatedUser cliente = user(Rol.CLIENTE, DOCUMENTO);

        assertTrue(cliente.isClienteWithDocumento(DOCUMENTO));
        assertFalse(cliente.isClienteWithDocumento("555555555"));
        assertFalse(cliente.isClienteWithDocumento(null));
    }

    @Test
    void staffNeverActsAsClient() {
        assertFalse(user(Rol.ASESOR, DOCUMENTO).isClienteWithDocumento(DOCUMENTO));
        assertFalse(user(Rol.ADMIN, null).isClienteWithDocumento(null));
    }

    private AuthenticatedUser user(Rol rol, String documento) {
        return new AuthenticatedUser(UUID.randomUUID(), "user@example.com", rol, documento);
    }
}
