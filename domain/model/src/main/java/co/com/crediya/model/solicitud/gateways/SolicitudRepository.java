package co.com.crediya.model.solicitud.gateways;

import co.com.crediya.model.solicitud.Solicitud;

public interface SolicitudRepository {
    Solicitud save(Solicitud solicitud);
}
