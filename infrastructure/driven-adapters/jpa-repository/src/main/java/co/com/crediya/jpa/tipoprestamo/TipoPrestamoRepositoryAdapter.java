package co.com.crediya.jpa.tipoprestamo;

import co.com.crediya.model.solicitud.gateways.TipoPrestamoRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TipoPrestamoRepositoryAdapter implements TipoPrestamoRepository {
    private final TipoPrestamoDataRepository repository;

    public TipoPrestamoRepositoryAdapter(TipoPrestamoDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return repository.existsById(nombre);
    }
}
