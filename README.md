# UrbanDelivery

Plataforma web de logística urbana local para pueblos del interior: digitaliza el catálogo de comercios, desacopla el reparto con cadetes independientes y permite mandados generales.

> Trabajo final de facultad — inventiva propia.

## Equipo

| Rol | Nombre |
|-----|--------|
| Integrante | Hillar, Saul |
| Integrante | Diaz, Agustin |
| Tutor | Londero, Oscar |

## Documentación

- [Propuesta de proyecto (1.ª entrega)](docs/01-propuesta.md)
- [Actualización post-devolución del tutor](docs/02-actualizacion.md)
- [Casos de pedido y máquinas de estado](docs/03-casos-y-estados.md)
- [Modelo Entidad-Relación](docs/04-modelo-er.md)
- [Listado de módulos](docs/05-modulos.md)
- [Relevamiento (Forms anónimos)](docs/relevamiento/README.md)
- [Guía de entrevista a cadetes](docs/relevamiento/guia-cadetes.md)

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Java + Spring Boot (API REST) |
| Seguridad | Spring Security + JWT + roles |
| Base de datos | PostgreSQL + Spring Data JPA |
| Frontend | React + Tailwind CSS + TanStack Query |
| Infra | Docker; deploy previsto en Render (API/DB) + Vercel (front) |

## Estructura del repositorio

```
backend/             # API Spring Boot 4 (Java 21, Maven Wrapper)
frontend/            # SPA React + Vite + Tailwind + TanStack Query
docs/                # Propuesta, casos y estados, ER y módulos
docker-compose.yml   # PostgreSQL 16 para desarrollo
```

## Cómo levantarlo

Requisitos: **JDK 21**, **Docker** y **Node 22** con pnpm (o npm).

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

Usuarios de demostración (contraseña `demo1234`): `cliente@urbandelivery.local`, `rotiseria@urbandelivery.local`, `farmacia@urbandelivery.local`. Se cargan desde `backend/src/main/resources/db/demo`.

**Tests:** `cd backend && ./mvnw verify` corre los tests unitarios y los de integración contra un PostgreSQL efímero (Testcontainers, requiere Docker). Sin Docker se puede apuntar a una base existente: `./mvnw verify -Dtest.external-db=true -DDB_URL=jdbc:postgresql://localhost:5432/urbandelivery`.

## Estado actual

**1.ª entrega:** propuesta, plan de trabajo, stack y repositorio.

**Actualización (Ago 2026):** relevamiento de campo (11 consumidores + 5 comercios, Forms anónimos) y respuesta a devolución del tutor — ver [docs/02-actualizacion.md](docs/02-actualizacion.md).

**2.ª entrega (Sep 2026):** casos de pedido y máquinas de estado ([03](docs/03-casos-y-estados.md)), modelo ER ([04](docs/04-modelo-er.md)) y listado de módulos ([05](docs/05-modulos.md)).

**3.ª devolución (Oct 2026): Modelo y Arquitectura conforme con correcciones.** Correcciones aplicadas en [03](docs/03-casos-y-estados.md) (dinero, mínima información para el cadete, única localidad, fallback PICKUP).

**Primer recorrido ejecutable (caso B, PICKUP):** cliente → comercio → pedido → acepta → prepara → `READY` → retira → `COMPLETED`, con login JWT, máquina de estados e historial en la misma transacción, probado de punta a punta contra PostgreSQL.

Pendiente: entrevistas a 3–5 cadetes para validar el actor COURIER. Si se valida, sigue el caso A (DELIVERY) con prueba concurrente real al tomar la entrega.
