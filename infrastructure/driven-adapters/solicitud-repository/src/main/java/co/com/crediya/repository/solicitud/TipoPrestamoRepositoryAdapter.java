package co.com.crediya.repository.solicitud;

import co.com.crediya.model.solicitud.gateways.TipoPrestamoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TipoPrestamoRepositoryAdapter implements TipoPrestamoRepository {
    private static final String EXISTS_BY_NOMBRE =
            "SELECT EXISTS (SELECT 1 FROM tipos_prestamo WHERE nombre = ?)";

    private final JdbcTemplate jdbcTemplate;

    public TipoPrestamoRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByNombre(String nombre) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_NOMBRE, Boolean.class, nombre);
        return Boolean.TRUE.equals(exists);
    }
}
