package co.com.crediya.model.auth;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId, String correoElectronico, Rol rol, String documentoIdentidad) {

    /** Un usuario solo actúa a nombre de un documento si es CLIENTE y ese documento es el suyo. */
    public boolean isClienteWithDocumento(String documento) {
        return rol == Rol.CLIENTE && documento != null && documento.equals(documentoIdentidad);
    }
}
