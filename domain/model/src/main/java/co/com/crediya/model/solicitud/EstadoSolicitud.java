package co.com.crediya.model.solicitud;

public enum EstadoSolicitud {
    PENDIENTE_DE_REVISION("Pendiente de revisión", true),
    REVISION_MANUAL("Revisión manual", true),
    RECHAZADA("Rechazada", true),
    APROBADA("Aprobada", false);

    private final String descripcion;
    private final boolean revisable;

    EstadoSolicitud(String descripcion, boolean revisable) {
        this.descripcion = descripcion;
        this.revisable = revisable;
    }

    /** Si una solicitud en este estado aparece en el listado de revisión manual. */
    public boolean esRevisable() {
        return revisable;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
