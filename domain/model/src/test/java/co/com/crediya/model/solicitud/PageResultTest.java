package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void computesTotalPages() {
        assertEquals(2, new PageResult<>(List.of("a"), 1, 5, 6).totalPages());
        assertEquals(0, new PageResult<>(List.of(), 0, 5, 0).totalPages());
    }

    @Test
    void rejectsInvalidPages() {
        assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), -1, 5, 0));
        assertThrows(IllegalArgumentException.class, () -> new PageResult<>(List.of(), 0, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> new PageResult<String>(null, 0, 5, 0));
    }
}
