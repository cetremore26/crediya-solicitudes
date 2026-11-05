package co.com.crediya.model.solicitud.gateways;

public interface ClienteGateway {
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
