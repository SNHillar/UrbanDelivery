# UrbanDelivery

Plataforma web de pedidos para comercios de pueblos del interior: catálogo digital, carrito mono-comercio y seguimiento del pedido hasta el retiro en el local.

**Informe Final:** [docs/Informe-Final-UrbanDelivery.pdf](docs/Informe-Final-UrbanDelivery.pdf) · **Video:** https://www.youtube.com/watch?v=bnhVlSJ-ngU

**Sistema desplegado:** https://urban-delivery-three.vercel.app — usuarios de demostración más abajo (contraseña `demo1234`). La primera carga puede tardar 30–60 s: el backend en el plan gratuito de Render se duerme sin tráfico.

> Trabajo final de facultad — inventiva propia. **Alcance de la entrega final: V1 solo retiro (PICKUP)** — el reparto con cadetes independientes y los mandados generales quedaron fuera de esta entrega; ver [por qué y qué significa](docs/06-alcance-final.md).

## Equipo

| Rol | Nombre |
|-----|--------|
| Integrante | Hillar, Saul |
| Integrante | Diaz, Agustin |
| Tutor | Londero, Oscar |

## Documentación

- [Informe Final (PDF)](docs/Informe-Final-UrbanDelivery.pdf)
- [Propuesta de proyecto (1.ª entrega)](docs/01-propuesta.md)
- [Actualización post-devolución del tutor](docs/02-actualizacion.md)
- [Casos de pedido y máquinas de estado](docs/03-casos-y-estados.md)
- [Modelo Entidad-Relación](docs/04-modelo-er.md)
- [Listado de módulos](docs/05-modulos.md)
- [Alcance de la entrega final: V1 solo PICKUP](docs/06-alcance-final.md)
- [Deploy: Render (API + DB) y Vercel (front)](docs/07-deploy.md)
- [Relevamiento (Forms anónimos)](docs/relevamiento/README.md)
- [Guía de entrevista a cadetes](docs/relevamiento/guia-cadetes.md)
- [Backend: arquitectura, endpoints, errores y tests](backend/README.md)
- [Frontend: estructura, rutas y recorrido de prueba](frontend/README.md)

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Java + Spring Boot (API REST) |
| Seguridad | Spring Security + JWT + roles |
| Base de datos | PostgreSQL + Spring Data JPA |
| Frontend | React + Tailwind CSS + TanStack Query |
| Infra | Docker; deploy en Render (API + PostgreSQL) y Vercel (front) |

## Estructura del repositorio

```
backend/             # API Spring Boot 4 (Java 21, Maven Wrapper)
frontend/            # SPA React + Vite + Tailwind + TanStack Query
docs/                # Propuesta, casos y estados, ER y módulos
docker-compose.yml   # PostgreSQL 16 para desarrollo
```

## Cómo levantarlo

Requisitos: **JDK 21**, **Docker** y **Node 22** con pnpm (o npm).

Con Docker Desktop en Linux, el daemon tiene que estar iniciado antes del paso 1 (`systemctl --user start docker-desktop` o abriendo la app). Si no, `docker compose` falla con `failed to connect to the docker API` y la API no arranca por falta de base.

```bash
# 1. Base de datos
docker compose up -d db

# 2. API (http://localhost:8080 · Swagger en /swagger-ui.html)
cd backend
./mvnw spring-boot:run

# 3. Frontend (http://localhost:5173)
cd frontend
pnpm install
pnpm dev
```

Usuarios de demostración (contraseña `demo1234`): `cliente@urbandelivery.local`, `rotiseria@urbandelivery.local`, `farmacia@urbandelivery.local`, `pendiente@urbandelivery.local` (comercio sin aprobar) y `admin@urbandelivery.local`. Se cargan desde `backend/src/main/resources/db/demo`. También se puede crear una cuenta nueva (cliente o comercio) desde `/registro`; un comercio nuevo queda `PENDING` hasta que el ADMIN lo aprueba en `/admin/comercios`.

Detalle de configuración, problemas comunes y recorrido de prueba en [backend/README.md](backend/README.md) y [frontend/README.md](frontend/README.md).

**Tests:** `cd backend && ./mvnw verify` corre los tests unitarios y los de integración contra un PostgreSQL efímero (Testcontainers, requiere Docker). Sin Docker se puede apuntar a una base existente: `./mvnw verify -Dtest.external-db=true -DDB_URL=jdbc:postgresql://localhost:5432/urbandelivery`.

## Estado actual

**1.ª entrega:** propuesta, plan de trabajo, stack y repositorio.

**Actualización (Ago 2026):** relevamiento de campo (11 consumidores + 5 comercios, Forms anónimos) y respuesta a devolución del tutor — ver [docs/02-actualizacion.md](docs/02-actualizacion.md).

**2.ª entrega (Sep 2026):** casos de pedido y máquinas de estado ([03](docs/03-casos-y-estados.md)), modelo ER ([04](docs/04-modelo-er.md)) y listado de módulos ([05](docs/05-modulos.md)).

**3.ª devolución (Oct 2026): Modelo y Arquitectura conforme con correcciones.** Correcciones aplicadas en [03](docs/03-casos-y-estados.md) (dinero, mínima información para el cadete, única localidad, fallback PICKUP).

**Recorrido ejecutable (caso B, PICKUP):** cliente → comercio → pedido → acepta → prepara → `READY` → retira → `COMPLETED`, con login JWT, máquina de estados e historial en la misma transacción, probado de punta a punta contra PostgreSQL.

**Moderación (ADMIN):** altas de comercio en `PENDING` → el ADMIN aprueba o rechaza desde `/admin/comercios`; recién ahí el comercio aparece en el catálogo público.

**Registro propio:** `/registro` da de alta cliente o comercio (con JWT inmediato); un comercio nuevo entra `PENDING` y sigue el mismo circuito de moderación de arriba.

`./mvnw verify`: 21 tests en verde con Testcontainers.

**Entrega final (Nov 2026): alcance cerrado en V1 solo PICKUP** — ver [docs/06-alcance-final.md](docs/06-alcance-final.md). El reparto con cadetes (caso A, DELIVERY) no se implementa en este trabajo final; queda documentado como evolución futura, no como P1 pendiente.

**Deploy:** API y PostgreSQL en Render (Docker), frontend en Vercel: https://urban-delivery-three.vercel.app. Pasos en [docs/07-deploy.md](docs/07-deploy.md).
