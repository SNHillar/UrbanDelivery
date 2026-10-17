# UrbanDelivery — API (backend)

API REST del recorrido PICKUP (caso B de [docs/03](../docs/03-casos-y-estados.md)): el cliente pide, el comercio acepta, prepara, marca listo y entrega en el local. El reparto (DELIVERY) todavía no está habilitado: depende de validar el actor COURIER con las entrevistas.

## Stack

| | |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA / Hibernate 7, Validation, Actuator) |
| Seguridad | Spring Security 7 como *resource server* con JWT HS256 firmado por la propia API |
| Base de datos | PostgreSQL 16, esquema versionado con Flyway |
| Documentación | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5, MockMvc, Testcontainers |
| Build | Maven Wrapper (`./mvnw`, no hace falta instalar Maven) |

## Cómo correrlo

Requisitos: JDK 21 y Docker (con Docker Desktop en Linux, iniciarlo antes: `systemctl --user start docker-desktop`).

```bash
# desde la raíz del repo
docker compose up -d db

cd backend
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (botón *Authorize* → pegar el token del login)
- Salud: `http://localhost:8080/actuator/health`

Para frenar: `Ctrl+C` (o `pkill -f spring-boot:run`) y `docker compose stop db`. Para borrar la base y empezar de cero: `docker compose down -v`.

### Configuración

Todo se lee de variables de entorno, con valores por defecto para desarrollo (ver [.env.example](.env.example) y `src/main/resources/application.yml`):

| Variable | Default | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/urbandelivery` | Conexión JDBC |
| `DB_USER` / `DB_PASSWORD` | `urbandelivery` / `urbandelivery` | Credenciales (las mismas que `docker-compose.yml`) |
| `JWT_SECRET` | secreto de desarrollo | Clave HMAC; mínimo 32 caracteres. **Cambiarlo fuera de desarrollo** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Orígenes del frontend |

Otras decisiones de configuración:

- `ddl-auto: validate`: Hibernate no crea tablas; solo verifica que las entidades coincidan con lo que dejó Flyway.
- `open-in-view: false`: no hay consultas perezosas desde los controladores; los repositorios traen lo necesario con `@EntityGraph`.
- Mensajes de validación siempre en español (`spring.web.locale: es`, resolver fijo).
- Los tokens duran 8 h (`app.jwt.ttl`).

### Problemas comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `failed to connect to the docker API at unix:///var/run/docker.sock` | Docker Desktop no está iniciado | `systemctl --user start docker-desktop` (o abrir la app) |
| La API corta al arrancar con `Connection refused` a `localhost:5432` | No está levantada la base | `docker compose up -d db` |
| `Port 8080 was already in use` | Quedó otra instancia corriendo | `pkill -f spring-boot:run` |
| Flyway falla con *checksum mismatch* | Se editó una migración ya aplicada | No editar migraciones aplicadas; crear una nueva. En desarrollo: `docker compose down -v` |

## Base de datos

Migraciones en `src/main/resources/`:

- `db/migration/V1__core_schema.sql`: tablas `users`, `merchants`, `products`, `orders`, `order_items`, `order_status_history`, con los CHECK de las invariantes de [docs/04](../docs/04-modelo-er.md) §3 (DELIVERY con dirección, PICKUP sin costo de envío, rechazo con motivo, etc.). `deliveries` y `courier_profiles` **no** se crean todavía: entran en una migración nueva si se valida COURIER.
- `db/demo/V1_1__demo_data.sql`: datos de demostración. En producción se carga solo `db/migration` (cambiar `spring.flyway.locations`).

### Usuarios de demostración

Contraseña de todos: `demo1234`.

| Email | Rol | Comercio |
|---|---|---|
| `cliente@urbandelivery.local` | CLIENT | — |
| `cliente2@urbandelivery.local` | CLIENT | — |
| `rotiseria@urbandelivery.local` | MERCHANT | Rotisería La Esquina (aprobado) |
| `farmacia@urbandelivery.local` | MERCHANT | Farmacia Central (aprobado) |
| `pendiente@urbandelivery.local` | MERCHANT | Verdulería Don José (pendiente de aprobación, no aparece en el listado) |
| `admin@urbandelivery.local` | ADMIN | — |

"Matambre a la pizza" de la rotisería está cargado como no disponible, para probar el caso C.

## Arquitectura

Paquetes por módulo (según [docs/05](../docs/05-modulos.md)), cada uno con `controller → service → repository`:

```
ar.urbandelivery
├── common    ApiException, GlobalExceptionHandler (ProblemDetail), Clock y OpenAPI
├── auth      Login, emisión/validación de JWT, SecurityConfig, AuthenticatedUser
├── user      User, Role
├── merchant  Merchant, listado de comercios aprobados
├── catalog   Product, catálogo público y disponibilidad para el comercio
├── order     Order, OrderItem, OrderStatus (máquina de estados), OrderService
└── history   OrderStatusHistory, StatusHistoryService
```

Decisiones de diseño:

- **La máquina de estados vive en el dominio.** `OrderStatus` tiene la tabla de transiciones permitidas de docs/03 §2 y `Order` expone acciones (`accept`, `reject`, `markReady`, `completePickup`, `cancel`). Una transición no permitida lanza `409 INVALID_TRANSITION`. Los servicios no cambian el estado a mano.
- **Historial en la misma transacción.** `StatusHistoryService.record` usa `Propagation.MANDATORY`: si alguien lo llama fuera de una transacción falla, así que no puede quedar un cambio de estado sin su fila de historial (invariante #11). La creación del pedido también registra `null → PENDING`.
- **Concurrencia con bloqueo optimista.** `orders.version` + `@Version`. Si dos operaciones modifican el mismo pedido a la vez, una gana y la otra recibe `409 CONCURRENT_MODIFICATION`. Es el mismo mecanismo que se va a usar cuando dos cadetes intenten tomar la misma entrega.
- **Precios congelados.** Al crear el pedido se copian `product_name` y `unit_price` en `order_items`; cambiar el catálogo después no altera pedidos existentes.
- **Dinero.** UrbanDelivery no cobra productos: solo registra `subtotal`, `delivery_fee` (siempre 0 en PICKUP) y `total`.
- **`history` no depende de `order`.** `OrderStatusHistory` guarda `orderId` como número, para no crear un ciclo entre paquetes.
- **Autorización en dos niveles.** El rol se chequea con `@PreAuthorize`; la propiedad (que el pedido sea del cliente o del comercio del usuario) se chequea en el servicio (invariante #12).

## Endpoints

Todos bajo `/api`. Salvo login y lectura de comercios/catálogo, requieren `Authorization: Bearer <token>`.

| Método | Ruta | Rol | Qué hace |
|---|---|---|---|
| POST | `/auth/login` | público | `{email, password}` → `{token, expiresAt, user}` |
| GET | `/auth/me` | autenticado | Datos del usuario del token (incluye `merchantId` si es comercio) |
| GET | `/merchants` | público | Comercios aprobados |
| GET | `/merchants/{id}` | público | Detalle de un comercio aprobado |
| GET | `/merchants/{id}/products` | público | Catálogo del comercio |
| GET | `/merchant/products` | MERCHANT | Productos del comercio propio (incluye no disponibles) |
| PATCH | `/merchant/products/{id}/availability` | MERCHANT | `{available}`: marca disponible / no disponible |
| POST | `/orders` | CLIENT | Crea un pedido PICKUP: `{merchantId, fulfillmentType, notes?, items:[{productId, quantity}]}` |
| GET | `/orders/mine` | CLIENT | Pedidos del cliente |
| GET | `/merchant/orders?status=` | MERCHANT | Bandeja del comercio, filtrable por estado |
| GET | `/orders/{id}` | dueño | Pedido + historial de estados + etiqueta para el cliente |
| POST | `/orders/{id}/accept` | MERCHANT | `PENDING → PREPARING` |
| POST | `/orders/{id}/reject` | MERCHANT | `PENDING → REJECTED`, requiere `{reason}` |
| POST | `/orders/{id}/ready` | MERCHANT | `PREPARING → READY` |
| POST | `/orders/{id}/complete` | MERCHANT | `READY → COMPLETED` (el cliente retiró) |
| POST | `/orders/{id}/cancel` | CLIENT / MERCHANT | Cliente: solo en `PENDING`. Comercio: en `PREPARING` o `READY`, con `{reason}` |

### Errores

Todas las respuestas de error son `application/problem+json` (RFC 9457) con una propiedad `code` estable para que el frontend no dependa del texto:

| HTTP | `code` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Falla Bean Validation; detalle por campo en `errors` |
| 400 | `MALFORMED_REQUEST` | JSON inválido |
| 400 | `REASON_REQUIRED` | Rechazo o cancelación del comercio sin motivo |
| 401 | `BAD_CREDENTIALS` | Login incorrecto (mismo mensaje si falla el email o la contraseña) |
| 401 | — | Sin token o token vencido/inválido |
| 403 | `FORBIDDEN` | Rol incorrecto o el recurso no es del usuario |
| 404 | `NOT_FOUND` | Comercio, producto, pedido o usuario inexistente |
| 409 | `INVALID_TRANSITION` | La máquina de estados no permite ese paso |
| 409 | `CLIENT_CANCEL_NOT_ALLOWED` | El cliente quiere cancelar un pedido ya aceptado |
| 409 | `USE_REJECT` | El comercio quiere "cancelar" un pedido `PENDING` (se rechaza, no se cancela) |
| 409 | `NOT_PICKUP` | `complete` sobre un pedido que no es de retiro |
| 409 | `CONCURRENT_MODIFICATION` | Otra operación modificó el pedido al mismo tiempo |
| 422 | `DELIVERY_NOT_AVAILABLE` | Se pidió envío; todavía no está habilitado |
| 422 | `PICKUP_NOT_OFFERED` | El comercio no ofrece retiro |
| 422 | `PRODUCT_NOT_IN_MERCHANT` | El producto es de otro comercio (pedido mono-comercio) |
| 422 | `PRODUCT_UNAVAILABLE` | Producto marcado como no disponible |

## Tests

```bash
./mvnw verify
```

Corre los tests unitarios (Surefire) y los de integración `*IT` (Failsafe). Los de integración levantan un PostgreSQL efímero con Testcontainers, así que necesitan Docker.

Para correrlos contra una base existente sin Testcontainers:

```bash
./mvnw verify -Dtest.external-db=true -DDB_URL=jdbc:postgresql://localhost:5432/urbandelivery
```

| Test | Qué cubre |
|---|---|
| `OrderStatusTest` | Todas las transiciones permitidas y prohibidas coinciden con docs/03 §2 |
| `AuthIT` | El login devuelve un JWT que identifica al usuario; contraseña incorrecta, token ausente o falsificado → 401; el catálogo público no pide token |
| `PickupOrderFlowIT` | Caso B completo con 4 filas de historial y actores correctos; caso C (rechazo sin motivo → 400, con motivo → `REJECTED`); el cliente cancela en `PENDING` pero no en `PREPARING`; el comercio cancela un pedido aceptado con motivo pero un `PENDING` se rechaza; otro comercio no puede actuar → 403; transición inválida → 409; producto no disponible, producto de otro comercio y DELIVERY → 422; un comercio no puede crear pedidos → 403; **dos aceptaciones simultáneas del mismo pedido → exactamente una 200 y una 409** |

Último resultado (17/10/2026): 14 tests en verde, tanto con Testcontainers como contra PostgreSQL local.

## Próximos pasos

Si las entrevistas validan COURIER: migración nueva con `courier_profiles` y `deliveries`, slice DELIVERY (caso A), bandeja del cadete con mínima información (comercio, zona aproximada y tarifa; dirección exacta recién al ser asignado) y test concurrente de dos cadetes tomando la misma entrega.
