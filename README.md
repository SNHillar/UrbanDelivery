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
backend/     # API Spring Boot (pendiente de desarrollo)
frontend/    # App React (pendiente de desarrollo)
docs/        # Propuesta, casos y estados, ER y módulos
```

## Estado actual

**1.ª entrega:** propuesta, plan de trabajo, stack y repositorio.

**Actualización (Ago 2026):** relevamiento de campo (11 consumidores + 5 comercios, Forms anónimos) y respuesta a devolución del tutor — ver [docs/02-actualizacion.md](docs/02-actualizacion.md).

**2.ª entrega (Sep 2026):** casos de pedido y máquinas de estado ([03](docs/03-casos-y-estados.md)), modelo ER ([04](docs/04-modelo-er.md)) y listado de módulos ([05](docs/05-modulos.md)).

Próximo hito (desde 28/09): fundación técnica — Spring Boot + PostgreSQL (Docker Compose), spike de seguridad (JWT + roles) y primer vertical slice.
