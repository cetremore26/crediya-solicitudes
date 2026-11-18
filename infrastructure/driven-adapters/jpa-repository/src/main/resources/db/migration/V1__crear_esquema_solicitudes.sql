-- Esquema inicial de solicitudes (el mismo que creaba schema.sql antes de usar Flyway).
CREATE TABLE tipos_prestamo (
    nombre VARCHAR(50) PRIMARY KEY,
    tasa_interes DECIMAL(5, 2) DEFAULT 0 NOT NULL
);

CREATE TABLE solicitudes (
    id UUID PRIMARY KEY,
    documento_identidad VARCHAR(15) NOT NULL,
    monto DECIMAL(17, 2) NOT NULL,
    plazo_meses INT NOT NULL,
    tipo_prestamo VARCHAR(50) NOT NULL,
    estado VARCHAR(40) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_solicitudes_tipo_prestamo FOREIGN KEY (tipo_prestamo)
        REFERENCES tipos_prestamo (nombre)
);

CREATE INDEX idx_solicitudes_estado ON solicitudes (estado, fecha_creacion);
CREATE INDEX idx_solicitudes_documento ON solicitudes (documento_identidad);
