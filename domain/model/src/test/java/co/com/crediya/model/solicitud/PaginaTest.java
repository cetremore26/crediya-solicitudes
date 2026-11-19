package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class PaginaTest {

    @Test
    void computesTotalPages() {
        assertEquals(2, new Pagina<>(List.of("a"), 1, 5, 6).totalPaginas());
        assertEquals(0, new Pagina<>(List.of(), 0, 5, 0).totalPaginas());
    }

    @Test
    void rejectsInvalidPages() {
        assertThrows(IllegalArgumentException.class, () -> new Pagina<>(List.of(), 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Pagina<>(List.of(), -1, 5, 0));
        assertThrows(IllegalArgumentException.class, () -> new Pagina<>(List.of(), 0, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> new Pagina<String>(null, 0, 5, 0));
    }
}
