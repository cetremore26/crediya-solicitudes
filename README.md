# crediya-solicitudes

Microservicio de solicitudes de préstamo de CrediYa (arquitectura hexagonal, Spring Boot).

## Endpoints
- `POST /api/v1/solicitud` (CLIENTE, a su nombre) registra una solicitud con estado inicial "Pendiente de revisión".
- `GET /api/v1/solicitud` (ASESOR) lista paginada de solicitudes para revisión manual.
- Swagger UI: `/swagger-ui.html`.

## Configuración

Los secretos **no tienen valor por defecto**: si faltan, el servicio no arranca. Para desarrollo
local activa el perfil `local` (`SPRING_PROFILES_ACTIVE=local`), que trae valores de desarrollo.

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | sí | Clave de PostgreSQL (`local`: `crediya_solicitudes_pass`, la del `docker-compose.yml`) |
| `SERVER_PORT` | no | `8081` |
| `USUARIOS_SERVICE_URL` | no | URL del microservicio de autenticación (`http://localhost:8080`) |
| `JWT_JWKS_URI` | no | Claves públicas para verificar tokens RS256 (`${USUARIOS_SERVICE_URL}/.well-known/jwks.json`) |
| `DB_URL` | no | `jdbc:postgresql://localhost:5433/crediya_solicitudes` |
| `DB_USERNAME` | no | `crediya` |
| `DB_DRIVER` / `DB_DIALECT` | no | `org.postgresql.Driver` / `org.hibernate.dialect.PostgreSQLDialect` |
| `DB_DDL_AUTO` | no | `none`: el esquema y los tipos de préstamo los crea `schema.sql` al arrancar |

Este servicio no tiene ninguna clave JWT: solo verifica tokens con la clave pública del servicio de autenticación.

Levanta la base con `docker compose up -d` desde la carpeta `CREDIYA`.
