# UrbanDelivery — Actualización post-devolución del tutor

| Campo | Dato |
|-------|------|
| **Proyecto** | UrbanDelivery |
| **Equipo** | Hillar, Saul · Diaz, Agustin |
| **Tutor** | Londero, Oscar |
| **Fecha** | Agosto 2026 |
| **Repositorio** | https://github.com/SNHillar/UrbanDelivery.git |

Documento de respuesta a la devolución de la 1.ª entrega. Incorpora relevamiento de campo, MVP corregido, reglas de negocio y viabilidad.

---

## 1. Síntesis del relevamiento

### Metodología

| Instrumento | Actor | N | Período | Notas |
|-------------|-------|---|---------|-------|
| Google Forms anónimo | Consumidores | **11** | 25/08/2026 | Encuesta múltiple choice + 1 pregunta abierta |
| Google Forms anónimo | Comerciantes | **5** | 25–28/08/2026 | Ramas según modalidad de venta |
| Entrevistas exploratorias | Consumidores | **3** | Ago 2026 | Charlas previas al Forms (cualitativas) |
| Contacto directo | Comerciante | **1** | Ago 2026 | Mensaje post-formulario (anonimizado) |
| Entrevista en profundidad | Farmacia | — | Pendiente | El dueño confirmó interés; charla pospuesta por disponibilidad |

**Limitaciones declaradas:** no se identificaron cadetes independientes en el relevamiento. La encuesta anónima priorizó respuesta sobre profundidad narrativa. Muestra acotada pero suficiente para detectar patrones; no es estudio de mercado formal.

### Hallazgos principales — demanda (N=11)

- **Modalidad:** 6 mezclan (van al local + encargan/piden envío); 5 solo van al local.
- **Dolores más frecuentes:** que no contesten o tarden (~6); no saber precios/stock/horarios (~5); no saber si el pedido ya viene (~3).
- **Sin envío:** 9/11 van ellos; 2 buscan otro comercio que sí envíe.
- **Disposición a pagar envío:** 5 sí · 5 depende · 1 no.
- **Montos razonables citados:** repartidos entre $500, $1000 y $2000.
- **¿Usarían una app del pueblo?** **11/11 sí** (8 condicionado a que funcione bien / no demore).
- **Valor percibido:** catálogo/precios, pedido sin WhatsApp, **estados del pedido**, cadete. Pago online aparece con frecuencia. GPS fue marginal (1 respuesta).

### Hallazgos principales — oferta (N=5)

| Tipo | Modalidad | Dolores | Envío hoy | ¿App? |
|------|-----------|---------|-----------|-------|
| Farmacia | Mezcla | Anotar pedidos; cliente no sabe si está listo | Conocido/cadete informal; dueño si no hay | Sí |
| Rotisería | WhatsApp + retiro | WhatsApp vs local; picos; cliente sin estado | A veces envían; en pico piden retiro o **rechazan** | Depende |
| Heladería | Mezcla | Falta envío estable; reparto y órdenes | Dueño cuando puede; comercio fija costo | Sí |
| Panadería | Mezcla | WhatsApp + local; anotar pedidos; inmediatez del cliente | Dueño; no cobran envío | Sí (app); envío depende costo |
| Veterinaria | Solo local | No encargos (tiempo, stock, sin cadete); pierde ventas | No opera envío | Depende |

**Patrones transversales (comercios):**

1. Cuatro de cinco ya operan con **WhatsApp o mezcla**; uno es solo mostrador pero reconoce ventas perdidas.
2. El dolor **“el cliente no sabe si está listo”** aparece en farmacia, rotisería y veterinaria.
3. **Conseguir quién lleve** es recurrente (farmacia, heladería, panadería).
4. En picos, la oferta **prioriza el salón** y posterga o rechaza pedidos externos (rotisería).
5. **Comisiones altas** es la barrera más citada; varios indicaron que probarían la app si el costo es razonable.
6. Operatoria actual: envío a menudo **sin cargo / incluido**; fallback = dueño entrega o cliente retira.

### Contacto cualitativo (comerciante, anonimizado)

> “Ojalá sea para hacer algo real en cuanto a página o algo que ordene. Hoy en día no se puede depender de las ganas o no ganas que tenga el personal para atender BIEN, COMO SE DEBE.”

Confirma **demanda de oferta** por un sistema que ordene pedidos, no solo difusión en redes.

### Cruce oferta × demanda

| Lo que piden los consumidores | Lo que confirman los comercios | Implicancia en UrbanDelivery |
|-------------------------------|--------------------------------|------------------------------|
| Que contesten / respondan rápido | WhatsApp + atender el local a la vez | Bandeja de pedidos estructurada |
| Ver estado del pedido | Cliente no sabe si está listo | Ciclo de estados visible (P0) |
| Catálogo y precios | Acomodar órdenes; picos | Catálogo + cola de pedidos |
| Envío a domicilio | Conseguir quién lleve | Panel de cadetes |
| Pago online (deseo) | No es prioridad en comercios hoy | Fuera del MVP (P2) |
| Confiabilidad (si demora, abandonan) | Rechazan/patean en picos | Merchant puede rechazar; estado transparente |

**Conclusión del relevamiento:** el problema existe en ambos lados. El foco validado no es “otro catálogo”, sino **ordenar el pedido, dar trazabilidad y conectar cadetes** en un contexto donde WhatsApp y la operatoria informal fallan.

---

## 2. Problema reescrito (con evidencia)

**Quién lo sufre**

- **Consumidores** que compran en almacenes, carnicerías, farmacias, rotiserías, etc., y dependen de ir al local, WhatsApp o encargos informales.
- **Comercios** que ya reciben pedidos por WhatsApp o mezclan mostrador + encargos, sin herramienta que ordene la cola ni comunique el estado al cliente.
- **Cadetes informales** (inferidos): cuando hay envío, lo resuelven el dueño o un conocido; no hay canal centralizado.

**Qué ocurre hoy**

1. El cliente consulta por WhatsApp, Instagram o yendo al local.
2. El comercio atiende mostrador y celular a la vez; anota pedidos a mano o los pierde.
3. El cliente no sabe si el pedido está en preparación, listo o en camino.
4. Si hay envío, se busca un cadete conocido o lo lleva el dueño; si no hay nadie, se pide retiro o se rechaza el pedido.
5. En horarios pico, algunos comercios priorizan el salón y postergan pedidos externos.

**Impacto**

- Demoras e incertidumbre para el consumidor (11/11 abiertos a una app; condicionan adopción a que funcione).
- Pérdida de ventas cuando el vecino no puede ir (veterinaria: “pasa seguido”).
- Sobrecarga operativa y dependencia del criterio del personal (contacto directo de comerciante).
- Entregas inconsistentes por falta de cadete disponible.

---

## 3. Propuesta de valor y diferenciación

Comparación con alternativas actuales (hipótesis validada parcialmente con N=11 + N=5):

| Alternativa | Qué resuelve | Qué limita | Aporte validado de UrbanDelivery |
|-------------|--------------|----------|----------------------------------|
| **WhatsApp** | Comunicación directa, conocida | Fragmentado; no contestan; sin estado claro; mezclado con atender el local | Pedido en un solo lugar + estados |
| **Ir al local** | Elegir producto; confianza | Tiempo; no siempre se puede ir | Catálogo + opción envío/retiro |
| **Cadete conocido** | Entrega puntual | Depende de contactos; no escalable | Lista de pedidos para cadetes del pueblo |
| **Plataformas masivas** | Marketplace + logística | Puede no ajustarse a pueblos chicos | Foco hiperlocal; comercios del pueblo |
| **Redes sociales** | Difusión | No estructuran pedido ni entrega | Catálogo operativo + flujo de pedido |

**Diferencial defendible para el MVP:** centralizar el pedido, hacer visible el estado y ofrecer un canal para que un cadete tome la entrega — sin depender de que alguien conteste bien un WhatsApp en hora pico.

**Segmento objetivo (acotado):** consumidores que ya encargan o quieren envío (mayoría del Forms) + comercios que ya usan WhatsApp/mezcla o expresaron interés en ordenar (4/5 + contacto directo). No apunta a quien prefiere ir siempre al local sin delivery (minoría).

---

## 4. Stack y experiencia del equipo

Stack mantenido. La elección se justifica por **continuidad con la cursada** y por cubrir backend, persistencia, seguridad y frontend SPA — aunque reconocemos **brechas honestas** de aprendizaje que mitigamos con alcance P0 y vertical slice.

### Experiencia previa (cursada y proyectos)

| Componente | Qué aporta al TF | Nivel del equipo | Detalle |
|------------|------------------|------------------|---------|
| **Java + Spring Boot** | API REST, capas, inyección de dependencias | **Intermedio (cursada)** | Visto en la materia: Spring Data JPA, Lombok, DevTools, Swagger/OpenAPI |
| **PostgreSQL + JPA** | Modelo relacional, repositorios | **Intermedio** | Bases relacionales y no relacionales vistas en la cursada; para UrbanDelivery usamos **PostgreSQL relacional** |
| **Spring Security + JWT** | Auth por roles (CLIENT, MERCHANT, COURIER, ADMIN) | **A aprender (Prog. IV U9–U10, ~nov)** | Login previo solo en front (localStorage). JWT en la materia: sem. 14–16; en el TF: Spring Security |
| **React + Tailwind + TanStack Query** | UI mobile-first, estado async | **A aprender (Prog. IV U5–U7, sep–oct)** | TS vanilla en cursada previa. React desde sem. 9; TanStack Query sem. 12 |
| **Docker** | Entorno local reproducible (API + BD) | **Básico / a consolidar** | No es el eje de la cursada previa; se usará con guías y compose mínimo |
| **Render + Vercel** | Deploy académico | **A aprender** | Objetivo de cierre (entrega final); desarrollo principalmente local con Docker |

### Por qué este stack igualmente

1. **Backend:** reutilizamos lo ya visto (Spring Boot + JPA) en lugar de introducir un lenguaje nuevo.
2. **Seguridad:** JWT/roles es exigencia del dominio (4 actores); aprenderlo en el TF es coherente con el nivel del trabajo final.
3. **Frontend:** React, Tailwind y TanStack Query están en el **programa oficial de Programación IV** (unidades 5–7); UrbanDelivery es donde se aplican.
4. **Alternativa descartada:** seguir solo con TS vanilla escalaría mal para roles, carrito y estados; React es la herramienta que la materia está introduciendo ahora.

### Alineación con Programación IV (2026)

El stack de UrbanDelivery coincide con la **Programación IV** (4.º cuatrimestre, inicio Agosto 2026). La materia contempla Spring Boot, JPA, React, TypeScript, Tailwind, TanStack Query, JWT y rutas protegidas por rol — mismos ejes que el TF, con backend en **Java/Spring** (unidades 1–2) y front en **React** (unidades 5 en adelante).

| Período | Unidad Prog. IV | Contenido relevante | Hito UrbanDelivery |
|---------|-----------------|---------------------|-------------------|
| Ago (sem. 1–2) | U1 | Spring Boot, beans, DI | Fundación backend |
| Ago (sem. 3–4) | U2 | REST, capas, JPA, Swagger | Modelo + API base |
| Sep (sem. 5–8) | U3–U4 | POO Python, FastAPI | *No usamos FastAPI en el TF*; foco en ER y módulos Java |
| **≤ 27/09** | ~sem. 8 | Primer parcial (FastAPI) | **2.ª entrega TF:** ER + listado de módulos |
| Sep–Oct (sem. 9–10) | U5 | React + Vite + TS + Tailwind | Inicio front (listados, formularios) |
| Oct (sem. 11–12) | U6–U7 | Relaciones BD, TanStack Query, React Router | Consumo API, pantallas por rol |
| Oct (sem. 13) | U8 | TanStack Form/Table, CRUD fullstack | Catálogo, carrito, tablas de pedidos |
| Nov (sem. 14–16) | U9–U10 | **JWT**, Zustand, **roles y rutas protegidas** | Auth Spring Security + guards en React |
| **≤ 14/11** | ~sem. 14–15 | Segundo parcial / cierre materia | **Entrega final TF** + deploy |

**Lectura:** el calendario académico **refuerza** el plan del TF, no lo contradice:

1. **Hasta el 27/09** el foco natural es backend + diseño (unidades 1–2 ya vistas o en curso).
2. **React y TanStack Query** entran cuando el TF necesita el front (octubre).
3. **JWT y roles** en la materia coinciden con **noviembre**, cuando el TF debe cerrar seguridad — el mayor aprendizaje nuevo está **calendarizado** en ambos lados.

**Diferencia honesta:** la cátedra enseña JWT con **FastAPI + Zustand**; UrbanDelivery lo implementará con **Spring Security + JWT** en backend y protección de rutas en React (conceptos equivalentes, distinta stack en auth del lado Python). El equipo traslada el patrón, no copia el mismo código de la unidad.

**RA de la materia que cubre el TF:**

| RA Prog. IV | Cómo lo cubre UrbanDelivery |
|-------------|----------------------------|
| RA2 — React, TS, Spring Boot | Front SPA + API REST Java |
| RA3 — JPA + BD relacional | PostgreSQL + entidades del dominio |
| RA4 — API REST + JWT | Endpoints de pedidos + auth por roles |

### Mayor riesgo de aprendizaje

| Riesgo | Mitigación |
|--------|------------|
| Spring Security + JWT + roles | Vertical slice de login temprano; documentación Spring; alcance P0 acotado |
| React + TanStack Query (nuevo) | Pantallas mínimas primero; reutilizar lógica ya pensada en TS vanilla (rutas, guards) |
| Deploy integrado | Docker local primero; cloud al cierre |

**Conclusión de viabilidad técnica:** el stack es **viable** si el MVP se mantiene en P0, se prioriza un flujo end-to-end antes de pulir UI, y se asume curva de aprendizaje en seguridad y React — no simultáneamente en cinco tecnologías nuevas sin orden.

---

## 5. MVP corregido (P0 / P1 / P2)

Corrección solicitada por el tutor: **mandados fuera del MVP obligatorio**.

### Flujo P0 (demo mínima)

`Cliente → Comercio → Pedido → Cadete → Entrega`

| Prioridad | Funcionalidad |
|-----------|---------------|
| **P0** | Auth JWT + roles |
| **P0** | Alta y aprobación ADMIN de MERCHANT / COURIER |
| **P0** | Catálogo por comercio |
| **P0** | Carrito mono-comercio |
| **P0** | Pedido con estados: Solicitado → En proceso → En camino → Entregado (+ Rechazado / Cancelado) |
| **P0** | Panel merchant: aceptar / rechazar pedidos |
| **P0** | Panel courier: ver disponibles, tomar, actualizar estado |
| **P1** | Mandados generales |
| **P1** | Búsqueda / filtros |
| **P1** | UX avanzada de reemplazo de carrito |
| **P2** | Pago online |
| **P2** | GPS / mapas |
| **P2** | Push, chat, ratings |

### Fuera de alcance (sin cambios)

Pagos online en MVP · Geolocalización · WebSockets/push · Chat · Multi-comercio por pedido · Ratings.

### Si el tiempo se reduce, se elimina primero

1. Mandados (P1)
2. Búsqueda y pulido UX
3. Código de verificación de entrega (P1)
4. Deploy cloud (mantener demo local)

---

## 6. Reglas de negocio críticas (flujo de pedido)

> **Reemplazado (Sep 2026):** tras la 2.ª devolución, los estados, las transiciones y las reglas de rechazo/cancelación y costo de envío se redefinieron en [03-casos-y-estados.md](03-casos-y-estados.md). Esta sección queda como registro histórico.

### Transiciones de estado

| Estado | Significado | Quién puede cambiarlo |
|--------|-------------|------------------------|
| **Solicitado** | Cliente creó el pedido | Cliente cancela |
| **En proceso** | Merchant aceptó; preparando | Merchant rechaza (→ Rechazado) |
| **En camino** | Courier tomó el pedido | Courier entrega o libera |
| **Entregado** | Fin exitoso | Courier |
| **Rechazado** | Merchant no puede/complica | Merchant (motivo corto) |
| **Cancelado** | Cliente o merchant cancela antes de salir | Cliente (hasta En proceso); Merchant (En proceso) |

### Decisiones argumentadas para el MVP

| Caso | Regla P0 |
|------|----------|
| Comercio rechaza (pico, sin stock) | Estado Rechazado + motivo. Evidencia: rotisería rechaza/patea pedidos externos. |
| Producto no disponible | Merchant rechaza (ajuste de ítems = P1). |
| Nadie toma el pedido | Queda En proceso; sin timeout automático en P0. |
| Dos cadetes toman el mismo pedido | Asignación atómica (un courier por pedido). **Invariante que no puede fallar.** |
| Cliente cancela | Solo hasta En proceso inclusive. |
| Courier abandona | Vuelve a En proceso sin courier asignado. |
| Costo de envío | Comercio carga monto fijo o “incluido / a acordar”. Sin cálculo por km. |
| Dirección | Texto + referencia. Sin mapa en P0. |
| Zona | Una localidad; fuera de zona → merchant rechaza. |
| Cadete ve antes de aceptar | Comercio, retiro, entrega, ítems, notas, costo envío. |

---

## 7. Viabilidad, riesgos y mitigaciones

| Dimensión | Evaluación | Riesgo principal | Mitigación |
|-----------|------------|------------------|------------|
| **Técnica** | Viable con stack propuesto | JWT, estados, concurrencia cadete | Vertical slice + tests de invariantes |
| **Conocimiento** | Viable; alineado a Prog. IV | JWT/roles (sem. 14–16); React (desde sem. 9) | Backend y ER antes del 27/09; front al ritmo de U5–U7; auth en noviembre |
| **Temporal** | Ajustada si se respeta P0 | Sobredimensionar alcance | P0/P1/P2 explícito; mandados fuera de demo |
| **Operativa** | Parcialmente validada | Adopción comercios; comisiones | Relevamiento muestra interés; sin comisiones altas en MVP académico |
| **Evidencia** | Mejorada vs 1.ª entrega | Muestra pequeña; sin cadetes entrevistados | Forms anónimos N=16 + contacto directo; entrevista farmacia pendiente |

---

## 8. Próximos pasos

1. ~~Completar experiencia previa del equipo en la sección 4.~~ *(hecho — Ago 2026)*
2. Referencia: programa oficial **Programación IV** (2026) — ver repositorio / adjunto de cátedra.
3. Entrevista cualitativa con farmacia (cuando haya disponibilidad).
4. Presentar actualización a Oscar para próxima revisión.
5. **2.ª entrega (≤ 27/09):** diagrama ER + listado de módulos, usando estas reglas de negocio.
6. Vertical slice técnico (post-aprobación): login → pedido → merchant → courier → entregado.

---

## Referencias en el repositorio

- [Propuesta inicial](01-propuesta.md)
- [Instrumentos de relevamiento](relevamiento/README.md)

**Referencia académica:** Programa de Cátedra — Programación IV (TUPAD, 4.º cuatrimestre 2026).
