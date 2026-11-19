package co.com.crediya.model.autenticacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UsuarioAutenticadoTest {
    private static final String DOCUMENTO = "1020304050";

    @Test
    void clientActsOnlyForItsOwnDocument() {
        UsuarioAutenticado cliente = user(Rol.CLIENTE, DOCUMENTO);

        assertTrue(cliente.esClienteConDocumento(DOCUMENTO));
        assertFalse(cliente.esClienteConDocumento("555555555"));
        assertFalse(cliente.esClienteConDocumento(null));
    }

    @Test
    void staffNeverActsAsClient() {
        assertFalse(user(Rol.ASESOR, DOCUMENTO).esClienteConDocumento(DOCUMENTO));
        assertFalse(user(Rol.ADMIN, null).esClienteConDocumento(null));
    }

    private UsuarioAutenticado user(Rol rol, String documento) {
        return new UsuarioAutenticado(UUID.randomUUID(), "user@example.com", rol, documento);
    }
}
