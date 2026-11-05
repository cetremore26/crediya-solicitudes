# crediya-solicitudes

Microservicio de solicitudes de préstamo de CrediYa (arquitectura hexagonal, Spring Boot).

## Endpoints
- `POST /api/v1/solicitud` registra una solicitud con estado inicial "Pendiente de revisión".
- Swagger UI: `/swagger-ui.html`.

## Configuración
- `SERVER_PORT` (8081 por defecto)
- `USUARIOS_SERVICE_URL` URL del microservicio de autenticación (http://localhost:8080 por defecto)
- Base de datos PostgreSQL (persistencia con JPA). Levántala con `docker compose up -d` desde la carpeta `CREDIYA`:
  - `DB_URL` (`jdbc:postgresql://localhost:5433/crediya_solicitudes` por defecto)
  - `DB_USERNAME` / `DB_PASSWORD` (`crediya` / `crediya_solicitudes_pass` por defecto, los del `docker-compose.yml`)
  - `DB_DRIVER` (`org.postgresql.Driver`) y `DB_DIALECT` (`org.hibernate.dialect.PostgreSQLDialect`)
  - `DB_DDL_AUTO` (`none`): el esquema y los tipos de préstamo los crea `schema.sql` al arrancar.
