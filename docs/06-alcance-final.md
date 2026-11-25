# UrbanDelivery — Alcance de la entrega final: V1 solo PICKUP

| Campo | Dato |
|-------|------|
| **Proyecto** | UrbanDelivery |
| **Equipo** | Hillar, Saul · Diaz, Agustin |
| **Tutor** | Londero, Oscar |
| **Fecha** | Noviembre 2026 |

Este documento deja asentada la decisión de cierre de alcance para la **entrega final** (≤ 21/11), posterior a los documentos de diseño de la 2.ª entrega ([03](03-casos-y-estados.md), [04](04-modelo-er.md), [05](05-modulos.md)).

## 1. Decisión

**La entrega final de UrbanDelivery cubre únicamente el recorrido PICKUP** (retiro en el local): cliente explora comercios, arma un pedido de un solo comercio, el comercio lo acepta/rechaza/prepara/marca listo, y el cliente lo retira. El reparto con cadetes (`COURIER`, caso DELIVERY) **no se implementa** en esta entrega.

## 2. Por qué

La 2.ª devolución del tutor ya había identificado el riesgo: el modelo de cadetes (bandeja común, asignación atómica, cobro de `delivery_fee`) dependía de validar con entrevistas que existiera una oferta real de cadetes dispuesta a trabajar así. Esas entrevistas ([guía](relevamiento/guia-cadetes.md)) no llegaron a concretarse antes del cierre de la materia. Ante esa falta de validación, el propio tutor había dejado planteada la alternativa en la 3.ª devolución: *"puede ser más razonable que el P0 se concentre inicialmente en PICKUP y que la red de reparto pase a una segunda etapa"*.

Como la entrega final es, en los hechos, el último hito del cursado — después no queda otra instancia de desarrollo antes de la defensa oral —, esa "segunda etapa" no se va a ejecutar dentro de este trabajo final. Se documenta entonces como **evolución futura fuera de alcance**, no como un P1 pendiente de una próxima entrega.

## 3. Qué significa esto en la práctica

- El esquema de `deliveries` y `courier_profiles` descrito en [04-modelo-er.md](04-modelo-er.md) **no se creó** en la base; `backend/src/main/resources/db/migration` solo tiene las tablas del caso PICKUP/rechazo.
- No hay bandeja de cadete, ni asignación de entregas, ni prueba de concurrencia entre couriers.
- Los "mandados generales" (P1 desde la 2.ª devolución) tampoco se implementan.
- Lo que **sí** se demuestra, probado de punta a punta (22 tests en verde, backend + integración):
  - Login JWT por rol; registro propio de cliente o comercio (`/registro`).
  - Alta de comercio pendiente (por registro o por dato semilla) → **ADMIN aprueba o rechaza** (módulo de moderación).
  - Cliente arma carrito mono-comercio y crea un pedido de retiro.
  - Comercio acepta, prepara, marca listo; cliente retira. Historial completo de trazabilidad.
  - Casos de rechazo (sin motivo → error; con motivo → `REJECTED`) y cancelación según quién y en qué estado.

Esto cubre los 4 criterios de éxito de la demo descritos en [01-propuesta.md §2.4](01-propuesta.md#24-criterios-de-éxito-del-mvp-demo) **reemplazando "cadete lo lleva a Entregado" por "comercio lo marca retirado"**, ya que el ciclo de vida del pedido PICKUP cierra sin necesidad de un cadete.

## 4. Para el Informe Final

Al escribir "objetivos y alcance" y "resultados y conclusiones" del informe, esto se redacta como:

- **Implementado y probado:** todo lo del punto 3.
- **Fuera de alcance / evolución futura (no implementado, no simulado):** reparto con cadetes independientes (DELIVERY), mandados generales, pagos online, geolocalización, notificaciones push/chat — estos últimos ya estaban fuera de alcance desde la propuesta inicial.

No debe presentarse el reparto con cadetes como "P1 si alcanza el tiempo": a esta altura, no alcanza el tiempo, y así se documenta.
