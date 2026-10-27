package co.com.crediya.repository.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class SolicitudRepositoryAdapterTest {
    private JdbcTemplate jdbcTemplate;
    private SolicitudRepositoryAdapter solicitudRepository;
    private TipoPrestamoRepositoryAdapter tipoPrestamoRepository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        solicitudRepository = new SolicitudRepositoryAdapter(jdbcTemplate);
        tipoPrestamoRepository = new TipoPrestamoRepositoryAdapter(jdbcTemplate);
    }

    @Test
    void savesSolicitudWithGeneratedIdAndPersistsIt() {
        Solicitud saved = solicitudRepository.save(solicitud("LIBRE_INVERSION"));

        assertNotNull(saved.getId());
        String estado = jdbcTemplate.queryForObject(
                "SELECT estado FROM solicitudes WHERE id = ?", String.class, saved.getId());
        assertEquals(EstadoSolicitud.PENDIENTE_DE_REVISION.name(), estado);
    }

    @Test
    void rejectsSolicitudWithUnknownLoanType() {
        Solicitud solicitud = solicitud("INEXISTENTE");

        assertThrows(
                DataIntegrityViolationException.class, () -> solicitudRepository.save(solicitud));
    }

    @Test
    void findsSeededLoanTypes() {
        assertTrue(tipoPrestamoRepository.existsByNombre("VEHICULO"));
        assertFalse(tipoPrestamoRepository.existsByNombre("INEXISTENTE"));
    }

    private Solicitud solicitud(String tipoPrestamo) {
        return Solicitud.builder()
                .documentoIdentidad("1020304050")
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .tipoPrestamo(tipoPrestamo)
                .estado(EstadoSolicitud.PENDIENTE_DE_REVISION)
                .build();
    }
}
