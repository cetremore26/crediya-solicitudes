package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DatosClienteTest {

    @Test
    void joinsNamesAndLastNames() {
        assertEquals("Ana Pérez", cliente("Ana", "Pérez").nombreCompleto());
    }

    @Test
    void skipsMissingParts() {
        assertEquals("Ana", cliente("Ana", " ").nombreCompleto());
        assertEquals("Pérez", cliente(null, "Pérez").nombreCompleto());
        assertNull(cliente(null, null).nombreCompleto());
    }

    private DatosCliente cliente(String nombres, String apellidos) {
        return new DatosCliente("1020304050", nombres, apellidos, "ana@example.com", null);
    }
}
