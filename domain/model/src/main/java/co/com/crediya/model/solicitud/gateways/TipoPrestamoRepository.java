package co.com.crediya.model.solicitud.gateways;

public interface TipoPrestamoRepository {
    boolean existsByNombre(String nombre);
}
