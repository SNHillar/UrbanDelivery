# UrbanDelivery — Listado de módulos (2.ª Entrega)

| Campo | Dato |
|-------|------|
| **Proyecto** | UrbanDelivery |
| **Equipo** | Hillar, Saul · Diaz, Agustin |
| **Tutor** | Londero, Oscar |
| **Fecha** | Septiembre 2026 |
| **Base** | [03-casos-y-estados.md](03-casos-y-estados.md) · [04-modelo-er.md](04-modelo-er.md) |

Junto con el [modelo ER](04-modelo-er.md), este documento completa el hito **≤ 27/09**. Parte del borrador de módulos de Diaz, Agustin, adaptado al modelo vigente tras la 2.ª devolución (ver [§5](#5-diferencias-con-la-versión-preliminar)).

> Nombres de paquetes, tablas y estados en inglés (código y base). Documentación en español.

---

## 1. Módulos del backend (Spring Boot, arquitectura por capas)

Cada módulo es un paquete con sus capas `controller → service → repository` y sus entidades JPA. Las reglas de estado viven en el servicio (dominio rico solo en invariantes clave, según [01-propuesta.md §3](01-propuesta.md#3-arquitectura-y-tecnologías)).

| # | Módulo (paquete) | Responsabilidad | Tablas | Prioridad |
|---|------------------|-----------------|--------|-----------|
| 1 | **Auth & Seguridad** (`auth`) | Registro, login, emisión y validación de JWT, `@PreAuthorize` por rol, verificación de propiedad (dueño del comercio, cadete asignado, autor del pedido) | `users` | P0 |
| 2 | **Usuarios y perfiles** (`user`) | Datos de cuenta, baja lógica (`active`), alta de perfil de cadete | `users`, `courier_profiles` | P0 |
| 3 | **Comercios** (`merchant`) | Alta y edición del comercio: dirección de retiro, horarios, modalidades (`offers_pickup` / `offers_delivery`), `delivery_fee` fija. Listado público de comercios aprobados | `merchants` | P0 |
| 4 | **Moderación** (`admin`) | Listar altas pendientes; aprobar / rechazar comercios y cadetes; cancelación provisoria de pedidos `IN_TRANSIT` | `merchants.approval_status`, `courier_profiles.approval_status` | P0 |
| 5 | **Catálogo** (`catalog`) | CRUD de productos del comercio, toggle rápido de `available`, baja lógica (`active`). Listado público por comercio | `products` | P0 |
| 6 | **Pedidos** (`order`) | Crear pedido desde el carrito del frontend: valida mono-comercio, productos disponibles, modalidad habilitada; copia nombre/precio y `delivery_fee`; calcula `subtotal` y `total` | `orders`, `order_items` | P0 |
| 7 | **Máquina de estados del pedido** (`order`) | Transiciones `PENDING → PREPARING → READY → COMPLETED`, más `REJECTED` / `CANCELLED`, con actor autorizado y motivo obligatorio donde corresponde | `orders.status` | P0 |
| 8 | **Entregas** (`delivery`) | Crear la entrega al aceptar un DELIVERY; bandeja común de cadetes; tomar (asignación atómica + `@Version`), liberar, retirar (solo si `READY`), entregar (cierra el pedido en la misma transacción) | `deliveries` | P0 |
| 9 | **Trazabilidad** (`history`) | Registrar cada transición (entidad, desde, hacia, actor, motivo); calcular el estado que ve el cliente ([03 §4](03-casos-y-estados.md#4-estado-que-ve-el-cliente-derivado)); endpoint consultable por polling | `order_status_history` | P0 |
| 10 | **Transversal** (`common`) | Manejo global de errores, DTOs de respuesta, configuración (CORS, OpenAPI/Swagger), seed de datos para la demo | — | P0 |
| 11 | **Mandados generales** (`errand`) | Pedido sin catálogo (texto libre) que reutiliza la máquina de entregas | tabla nueva `errands` | P1 |
| 12 | **Búsqueda y filtros** (`catalog`) | Filtro por nombre / rubro en comercios y productos | — | P1 |
| 13 | **Categorías de productos** (`catalog`) | Agrupar productos dentro del comercio | tabla nueva `categories` | P1 |

## 2. Módulos del frontend (React + Tailwind + TanStack Query)

Una SPA mobile-first con rutas protegidas por rol. Se arranca junto con el backend en el recorrido PICKUP (login JWT incluido), sin esperar a que cada tema aparezca en Programación IV (3.ª devolución).

| Área | Pantallas | Consume módulos | Prioridad |
|------|-----------|-----------------|-----------|
| **Pública / Auth** | Login, registro (elige rol), solicitud de alta de comercio / cadete | 1, 2, 3 | P0 |
| **Cliente** | Comercios, catálogo del comercio, carrito mono-comercio con aviso de reemplazo, checkout (retiro / envío), mis pedidos con estado | 3, 5, 6, 9 | P0 |
| **Comercio** | Bandeja de pedidos (aceptar, rechazar con motivo, marcar listo, marcar retirado, cancelar), gestión de catálogo y disponibilidad, datos del comercio | 3, 5, 7 | P0 |
| **Cadete** | Bandeja común de entregas disponibles (comercio, zona aproximada y `delivery_fee`; dirección exacta recién al tomar), mis entregas (liberar, retirar, entregar) | 8, 9 | P0 *(sujeto a validar COURIER)* |
| **Admin** | Altas pendientes, aprobar / rechazar, listados generales | 4 | P0 |

---

## 3. Módulo → invariantes

Referencias a la tabla de invariantes de [04-modelo-er.md §3](04-modelo-er.md#3-invariantes).

| Módulo | Invariantes que garantiza |
|--------|---------------------------|
| Auth & Seguridad | #12 (solo el actor autorizado cambia cada estado) |
| Moderación | Solo comercios y cadetes `APPROVED` operan (precondición de #5 y de tomar entregas) |
| Pedidos | #4 (mono-comercio), #5 (productos disponibles de comercio aprobado), #8 (precios congelados), #9 (dirección si DELIVERY; PICKUP sin envío) |
| Máquina de estados del pedido | #10 (rechazo con motivo) |
| Entregas | #1 (PICKUP sin entrega), #2 (una entrega por pedido), #3 (un solo cadete), #6 (retiro solo en READY), #7 (entregar cierra el pedido) |
| Trazabilidad | #11 (cada transición registrada con actor) |

## 4. Orden de implementación (vertical slice)

1. `common` + `auth` + `user`: Docker Compose con PostgreSQL, login y JWT, un endpoint protegido por rol (*spike* de seguridad temprano).
2. `merchant` + `catalog`: alta de comercio y productos; listado público.
3. `admin`: aprobación de comercio y cadete (necesario para la demo).
4. `order` + `history`: pedido PICKUP completo (caso B) → primer flujo punta a punta.
5. `delivery`: pedido DELIVERY con cadete (caso A), incluida la prueba de concurrencia al tomar.
6. Frontend por rol sobre los endpoints ya estables. P1 solo si sobra tiempo.

---

## 5. Diferencias con la versión preliminar

El borrador de módulos (Diaz, Agustin) se armó sobre las reglas de [02-actualizacion.md §6](02-actualizacion.md#6-reglas-de-negocio-críticas-flujo-de-pedido). La 2.ª devolución del tutor obligó a cambiar el modelo; el listado se conservó y se ajustó así:

| Borrador | Versión final | Motivo |
|----------|---------------|--------|
| Una máquina de estados: Solicitado → En proceso → En camino → Entregado | Dos máquinas: pedido (`orders`) y entrega (`deliveries`) | Que un cadete tome el envío no implica que el pedido esté en camino |
| Sin estado "listo" | `READY` explícito | Dolor "el cliente no sabe si está listo" (relevamiento) |
| Siempre con envío | `fulfillment_type` PICKUP / DELIVERY | Muchos clientes encargan y retiran |
| `courier_id` en `orders` | Tabla `deliveries` (0..1 por pedido) | La entrega solo existe para DELIVERY y tiene su propio ciclo |
| `delivery_cost` + nota "a acordar" | `delivery_fee` fija del comercio, copiada al pedido | Evitar reintroducir la negociación manual |
| Tabla `categories` | Módulo P1; `merchants.category` como texto en P0 | Acotar P0 |
| `couriers` con `id` propio | `courier_profiles` con `user_id` como PK | Perfil 1:1 del usuario |
| Paneles Comercio / Cadete / Admin como módulos backend | Pasan a la sección frontend; el backend se organiza por dominio | Los paneles son vistas sobre los mismos servicios |

Se mantienen del borrador: usuario único con rol, perfiles aprobados por ADMIN, carrito no persistido, snapshot de precios, historial de estados, polling en lugar de WebSockets, y los módulos P1 (mandados, búsqueda).
