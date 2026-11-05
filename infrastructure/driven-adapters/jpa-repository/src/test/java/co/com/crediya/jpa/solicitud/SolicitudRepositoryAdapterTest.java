package co.com.crediya.jpa.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.jpa.JpaTestApplication;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = JpaTestApplication.class)
class SolicitudRepositoryAdapterTest {
    private static final Set<EstadoSolicitud> ALL_REVIEW_STATES = Set.of(
            EstadoSolicitud.PENDIENTE_DE_REVISION,
            EstadoSolicitud.RECHAZADA,
            EstadoSolicitud.REVISION_MANUAL);

    @Autowired
    private SolicitudRepositoryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM solicitudes");
    }

    @Test
    void savesSolicitudWithGeneratedIdAndPersistsIt() {
        Solicitud saved = save("1020304050", "LIBRE_INVERSION", EstadoSolicitud.PENDIENTE_DE_REVISION);

        assertNotNull(saved.getId());
        String estado = jdbcTemplate.queryForObject(
                "SELECT estado FROM solicitudes WHERE id = ?", String.class, saved.getId());
        assertEquals(EstadoSolicitud.PENDIENTE_DE_REVISION.name(), estado);
    }

    @Test
    void keepsProvidedId() {
        UUID id = UUID.randomUUID();

        Solicitud saved = adapter.save(solicitud("1020304050", "VEHICULO",
                EstadoSolicitud.PENDIENTE_DE_REVISION).toBuilder().id(id).build());

        assertEquals(id, saved.getId());
    }

    @Test
    void rejectsSolicitudWithUnknownLoanType() {
        Solicitud solicitud = solicitud("1020304050", "INEXISTENTE", EstadoSolicitud.PENDIENTE_DE_REVISION);

        assertThrows(DataIntegrityViolationException.class, () -> adapter.save(solicitud));
    }

    @Test
    void listsOnlyReviewStatesWithLoanTypeRateOldestFirst() {
        save("111111", "VEHICULO", EstadoSolicitud.PENDIENTE_DE_REVISION);
        save("222222", "VIVIENDA", EstadoSolicitud.REVISION_MANUAL);
        save("333333", "VEHICULO", EstadoSolicitud.APROBADA);

        PageResult<Solicitud> page =
                adapter.findForReview(new FiltroRevision(ALL_REVIEW_STATES, null, 0, 10));

        assertEquals(2, page.totalElements());
        assertEquals("111111", page.content().get(0).getDocumentoIdentidad());
        assertEquals(0, new BigDecimal("14.00").compareTo(page.content().get(0).getTasaInteres()));
    }

    @Test
    void filtersByStateAndLoanType() {
        save("111111", "VEHICULO", EstadoSolicitud.PENDIENTE_DE_REVISION);
        save("222222", "VIVIENDA", EstadoSolicitud.PENDIENTE_DE_REVISION);
        save("333333", "VIVIENDA", EstadoSolicitud.RECHAZADA);

        PageResult<Solicitud> byType = adapter.findForReview(
                new FiltroRevision(ALL_REVIEW_STATES, "VIVIENDA", 0, 10));
        PageResult<Solicitud> byState = adapter.findForReview(
                new FiltroRevision(Set.of(EstadoSolicitud.RECHAZADA), null, 0, 10));

        assertEquals(2, byType.totalElements());
        assertEquals(1, byState.totalElements());
        assertEquals("333333", byState.content().get(0).getDocumentoIdentidad());
    }

    @Test
    void paginatesResults() {
        for (int i = 0; i < 5; i++) {
            save("10000" + i, "VEHICULO", EstadoSolicitud.PENDIENTE_DE_REVISION);
        }

        PageResult<Solicitud> secondPage =
                adapter.findForReview(new FiltroRevision(ALL_REVIEW_STATES, null, 1, 2));

        assertEquals(5, secondPage.totalElements());
        assertEquals(3, secondPage.totalPages());
        assertEquals(2, secondPage.content().size());
    }

    @Test
    void findsApprovedApplicationsOfGivenApplicants() {
        save("111111", "VEHICULO", EstadoSolicitud.APROBADA);
        save("111111", "VEHICULO", EstadoSolicitud.RECHAZADA);
        save("222222", "VEHICULO", EstadoSolicitud.APROBADA);

        List<Solicitud> approved = adapter.findApprovedByDocumentos(List.of("111111"));

        assertEquals(1, approved.size());
        assertTrue(approved.get(0).getTasaInteres().signum() > 0);
        assertTrue(adapter.findApprovedByDocumentos(List.of()).isEmpty());
    }

    private Solicitud save(String documento, String tipo, EstadoSolicitud estado) {
        return adapter.save(solicitud(documento, tipo, estado));
    }

    private Solicitud solicitud(String documento, String tipo, EstadoSolicitud estado) {
        return Solicitud.builder()
                .documentoIdentidad(documento)
                .monto(new BigDecimal("1000000"))
                .plazoMeses(12)
                .tipoPrestamo(tipo)
                .estado(estado)
                .build();
    }
}
