package co.com.crediya.repository.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class SolicitudConsultaRepositoryAdapterTest {
    private static final Set<EstadoSolicitud> ALL_REVIEW_STATES = Set.of(
            EstadoSolicitud.PENDIENTE_DE_REVISION,
            EstadoSolicitud.RECHAZADA,
            EstadoSolicitud.REVISION_MANUAL);

    private SolicitudRepositoryAdapter solicitudRepository;
    private SolicitudConsultaRepositoryAdapter consultaRepository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        solicitudRepository = new SolicitudRepositoryAdapter(jdbcTemplate);
        consultaRepository = new SolicitudConsultaRepositoryAdapter(jdbcTemplate);
    }

    @Test
    void listsOnlyReviewStatesWithLoanTypeRateOldestFirst() {
        save("111111", "VEHICULO", EstadoSolicitud.PENDIENTE_DE_REVISION);
        save("222222", "VIVIENDA", EstadoSolicitud.REVISION_MANUAL);
        save("333333", "VEHICULO", EstadoSolicitud.APROBADA);

        PageResult<Solicitud> page =
                consultaRepository.findForReview(new FiltroRevision(ALL_REVIEW_STATES, null, 0, 10));

        assertEquals(2, page.totalElements());
        assertEquals("111111", page.content().get(0).getDocumentoIdentidad());
        assertEquals(0, new BigDecimal("14.00").compareTo(page.content().get(0).getTasaInteres()));
    }

    @Test
    void filtersByStateAndLoanType() {
        save("111111", "VEHICULO", EstadoSolicitud.PENDIENTE_DE_REVISION);
        save("222222", "VIVIENDA", EstadoSolicitud.PENDIENTE_DE_REVISION);
        save("333333", "VIVIENDA", EstadoSolicitud.RECHAZADA);

        PageResult<Solicitud> byType = consultaRepository.findForReview(
                new FiltroRevision(ALL_REVIEW_STATES, "VIVIENDA", 0, 10));
        PageResult<Solicitud> byState = consultaRepository.findForReview(
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
                consultaRepository.findForReview(new FiltroRevision(ALL_REVIEW_STATES, null, 1, 2));

        assertEquals(5, secondPage.totalElements());
        assertEquals(3, secondPage.totalPages());
        assertEquals(2, secondPage.content().size());
    }

    @Test
    void findsApprovedApplicationsOfGivenApplicants() {
        save("111111", "VEHICULO", EstadoSolicitud.APROBADA);
        save("111111", "VEHICULO", EstadoSolicitud.RECHAZADA);
        save("222222", "VEHICULO", EstadoSolicitud.APROBADA);

        List<Solicitud> approved = consultaRepository.findApprovedByDocumentos(List.of("111111"));

        assertEquals(1, approved.size());
        assertTrue(approved.get(0).getTasaInteres().signum() > 0);
        assertTrue(consultaRepository.findApprovedByDocumentos(List.of()).isEmpty());
    }

    private void save(String documento, String tipo, EstadoSolicitud estado) {
        solicitudRepository.save(Solicitud.builder()
                .documentoIdentidad(documento)
                .monto(new BigDecimal("1000000"))
                .plazoMeses(12)
                .tipoPrestamo(tipo)
                .estado(estado)
                .build());
    }
}
