# crediya-solicitudes

Microservicio de solicitudes de préstamo de CrediYa. Registra las solicitudes de los clientes y
lista para los asesores las que necesitan revisión manual.

Sigue la Clean Architecture del scaffold de Bancolombia
([Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)).
Las dependencias solo apuntan hacia el dominio, y `DependenciasEntreCapasTest` (ArchUnit) hace
fallar el build si alguna capa lo incumple.

| Módulo | Capa | Contenido |
|---|---|---|
| `domain/model` | Dominio | `Solicitud`, `SolicitudConTasa`, `FiltroRevision`, `Pagina`, `EstadoSolicitud` y `UsuarioAutenticado`, con sus reglas, y los gateways |
| `domain/usecase` | Dominio | `RegistrarSolicitudUseCase` y `ListarSolicitudesRevisionUseCase` |
| `infrastructure/driven-adapters/jpa-repository` | Infraestructura | Persistencia en PostgreSQL con JPA; esquema y tipos de préstamo con Flyway (`db/migration`) |
| `infrastructure/driven-adapters/jwt-verifier` | Infraestructura | Verificación RS256 con el JWKS del servicio de autenticación |
| `infrastructure/driven-adapters/usuarios-client` | Infraestructura | Cliente HTTP del servicio de autenticación (existencia y datos de clientes) |
| `infrastructure/entry-points` | Infraestructura | Controladores REST, filtro JWT, seguridad y manejo de errores |
| `applications/app-service` | Aplicación | Arranque, configuración y registro automático de los `*UseCase` |

## Endpoints

| Método y ruta | Acceso | Descripción |
|---|---|---|
| `POST /api/v1/solicitud` | CLIENTE, a su nombre | Registra una solicitud con estado inicial "Pendiente de revisión" |
| `GET /api/v1/solicitud` | ASESOR | Lista paginada para revisión (`estado`, `tipo_prestamo`, `pagina`, `tamano` ≤ 50) con datos del cliente y su deuda mensual aprobada |
| `GET /actuator/health` | Público | Estado de salud (lo usan Docker y docker-compose) |
| `GET /swagger-ui.html` | Público | Documentación, solo con `SWAGGER_ENABLED=true` o en el perfil `local` |

Los errores siempre tienen el formato `{"estado": 422, "codigo": "CLIENTE_NO_ENCONTRADO", "mensaje": "..."}`.
Los propios de la integración con el servicio de autenticación son:

| Estado | Código | Cuándo |
|---|---|---|
| 422 | `CLIENTE_NO_ENCONTRADO` | El documento de la solicitud no corresponde a un usuario registrado |
| 502 | `INTEGRACION_USUARIOS_FALLIDA` | El servicio de usuarios rechazó la consulta (4xx) |
| 503 | `SERVICIO_NO_DISPONIBLE` | El servicio de usuarios no responde |
| 503 | `AUTENTICACION_NO_DISPONIBLE` | No se pudo descargar el JWKS para verificar el token |

## Configuración

Los secretos **no tienen valor por defecto**: si faltan, el servicio no arranca. Para desarrollo
local activa el perfil `local` (`SPRING_PROFILES_ACTIVE=local`), que trae valores de desarrollo.

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | sí | Clave de PostgreSQL (`local`: `crediya_solicitudes_pass`, la del `docker-compose.yml`) |
| `SERVER_PORT` | no | `8081` |
| `USUARIOS_SERVICE_URL` | no | URL del microservicio de autenticación (`http://localhost:8080`) |
| `JWT_JWKS_URI` | no | Claves públicas para verificar tokens RS256 (`${USUARIOS_SERVICE_URL}/.well-known/jwks.json`). Debe ser HTTPS |
| `JWT_ALLOW_INSECURE_JWKS_URI` | no | Permite un `JWT_JWKS_URI` por HTTP, solo en desarrollo o dentro de una red privada (`false`; `local`: `true`) |
| `SWAGGER_ENABLED` | no | Publica Swagger UI y `/v3/api-docs` (`false`; `local`: `true`) |
| `DB_URL` | no | `jdbc:postgresql://localhost:5433/crediya_solicitudes` |
| `DB_USERNAME` | no | `crediya` |
| `DB_DRIVER` / `DB_DIALECT` | no | `org.postgresql.Driver` / `org.hibernate.dialect.PostgreSQLDialect` |
| `DB_DDL_AUTO` | no | `validate`: el esquema y los tipos de préstamo los crean las migraciones de Flyway |

Este servicio no tiene ninguna clave JWT: solo verifica tokens con la clave pública del servicio de autenticación.

Levanta las bases y los servicios con `docker compose up -d` desde la carpeta `CREDIYA`.

## Migraciones

| Versión | Cambio |
|---|---|
| V1 | Tablas `tipos_prestamo` y `solicitudes` |
| V2 | Tipos de préstamo iniciales. Solo inserta los que falten, así que no pisa tasas ya ajustadas. Usa `MERGE`, que requiere **PostgreSQL 15 o superior** (el `docker-compose.yml` usa la 17) |

Las bases creadas antes de Flyway se registran en la versión 1 (`baseline-on-migrate`).

## Pruebas

```bash
./gradlew test      # unitarias, de adaptadores, de arquitectura e integración de la aplicación completa
./gradlew pitest    # pruebas de mutación (aparte, porque consumen mucha memoria)
```
