package co.com.crediya.jpa.solicitud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SolicitudEntityTest {

    @Test
    void isNewUntilPersistedOrLoadedEvenWithAnAssignedId() {
        SolicitudEntity entity = SolicitudEntity.builder().id(UUID.randomUUID()).build();

        assertTrue(entity.isNew(), "con id asignado sigue siendo nueva: INSERT sin SELECT previo");

        entity.marcarComoPersistida();

        assertFalse(entity.isNew());
    }
}
