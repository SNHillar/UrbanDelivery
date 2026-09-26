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
- [Relevamiento (Forms anónimos)](docs/relevamiento/README.md)
- [Esquema de BD y listado de módulos (2.ª entrega)](docs/03-modelo-datos-modulos.md)

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
docs/        # Propuesta, diseño BD y módulos
```

## Estado actual

**1.ª entrega:** propuesta, plan de trabajo, stack y repositorio.

**Actualización (Ago 2026):** relevamiento de campo (11 consumidores + 5 comercios, Forms anónimos) y respuesta a devolución del tutor — ver [docs/02-actualizacion.md](docs/02-actualizacion.md).

**2.ª entrega (Sep 2026):** esquema de base de datos (ER) y listado de módulos del backend — ver [docs/03-modelo-datos-modulos.md](docs/03-modelo-datos-modulos.md).

Próximo hito (28/09 – 07/11): desarrollo del MVP (fundación API, vertical slice del pedido, UI por roles).
