# UrbanDelivery — Propuesta de Proyecto (1.ª Entrega)

| Campo | Dato |
|-------|------|
| **Proyecto** | UrbanDelivery |
| **Tipo** | Inventiva propia |
| **Integrantes** | Hillar, Saul · Diaz, Agustin |
| **Tutor** | Londero, Oscar |
| **Fecha** | Agosto 2026 |
| **Repositorio** | Local: `C:\Users\saulh\Projects\UrbanDelivery` — _pegar URL de GitHub cuando crees el remoto_ |

---

## 1. Contexto y problema

En localidades del interior (~5.000 habitantes), el comercio local y los servicios de cadetería presentan una alta fragmentación operativa:

- **Comercios:** no cuentan con carta/catálogo digital ni infraestructura para venta online. Dependen de atender mensajes de WhatsApp manualmente en horarios pico y la mayoría no posee repartidores propios.
- **Consumidores:** deben consultar redes sociales o enviar mensajes a múltiples locales para averiguar horarios, productos disponibles y precios, con demoras e incertidumbre.
- **Cadetes independientes:** no existe un canal centralizado que conecte la oferta de reparto local con la demanda de vecinos y comercios.

---

## 2. Propuesta de solución (MVP)

**UrbanDelivery** es una plataforma web orientada a la logística urbana local que:

1. Digitaliza la oferta de comercios del pueblo (catálogo/menú).
2. Desacopla el reparto de los comercios, conectando pedidos y mandados con cadetes independientes.
3. Brinda trazabilidad simple del ciclo de vida de cada envío.

### 2.1 Actores y roles

| Rol | Capacidad principal |
|-----|---------------------|
| **CLIENT** | Explora comercios, realiza pedidos y solicita mandados. |
| **MERCHANT** | Administra catálogo/menú y gestiona pedidos entrantes. |
| **COURIER** | Visualiza entregas/mandados disponibles y actualiza estados de envío. |
| **ADMIN** | Modera y aprueba altas de comercios y cadetes. |

### 2.2 Funcionalidades del alcance (MVP)

- Gestión de usuarios y seguridad por roles (JWT).
- Alta de MERCHANT/COURIER con **aprobación de ADMIN** (confianza del sistema).
- Catálogo digital por comercio y exploración para el cliente.
- Carrito con **restricción de comercio único** y alerta de reemplazo.
- Pedidos de catálogo con estados: `Solicitado → En proceso → En camino → Entregado`.
- Módulo de **mandados generales** (envíos abiertos: farmacia, encargos, etc.).
- Panel de cadete para tomar trabajos y actualizar estado.

### 2.3 Fuera de alcance (explícito)

Para mantener viabilidad y plazos, el MVP **no incluye**:

- Pagos online (pago contra entrega / acuerdo offline).
- Geolocalización, mapas o ruteo.
- Notificaciones push / WebSockets (se usará consulta/polling).
- Chat in-app.
- Multi-comercio en un mismo pedido.
- Ratings, fidelización o analytics avanzados.

### 2.4 Criterios de éxito del MVP (demo)

Al cierre del desarrollo se debe poder demostrar:

1. Login por rol.
2. Admin aprueba un comercio y un cadete.
3. Cliente arma carrito de un solo comercio y crea un pedido.
4. Comercio procesa el pedido; cadete lo toma y lo lleva a **Entregado**.
5. (Si hay tiempo / P1) Cliente crea un mandado y un cadete lo completa.

---

## 3. Arquitectura y tecnologías

Arquitectura desacoplada (API REST + frontend SPA), apta para despliegue independiente.

| Capa | Tecnología |
|------|------------|
| **Backend** | Java + Spring Boot (API REST) |
| **Seguridad** | Spring Security + JWT + roles |
| **Persistencia** | Spring Data JPA + PostgreSQL (relacional) |
| **Diseño backend** | Arquitectura por capas; dominio rico solo en invariantes clave (estados de orden, carrito mono-comercio, aprobación) |
| **Frontend** | React + Tailwind CSS (mobile-first) + TanStack Query |
| **Contenedores** | Docker (API + base de datos en desarrollo) |
| **Despliegue** | Backend/DB en cloud (ej. Render); frontend en Vercel |
| **Control de versiones** | Un único repositorio GitHub del equipo |

---

## 4. Plan de trabajo (alineado a consignas)

| Ventana | Enfoque | Entregable |
|---------|---------|------------|
| **≤ 30/08** | Propuesta, stack y repositorio | **1.ª Entrega** |
| **≤ 27/09** | Esquema de BD (ER) + listado de módulos en el repo | **2.ª Entrega (condición de regular)** |
| **28/09 – 07/11** | Desarrollo del MVP: fundación API, *vertical slice* del pedido, UI por roles, mandados (P1) | Producto usable |
| **≤ 14/11** | Pruebas, Docker, despliegue online, informe escrito, video (preferente EN) | **Entrega final** |
| **Post 14/11** | Ensayo de presentación | **Defensa oral** |

### 4.1 Enfoque de desarrollo (post 2.ª entrega)

1. **Fundación:** Spring Boot + PostgreSQL (Docker Compose) + JWT/roles.
2. **Vertical slice temprano:** registro/login → listar comercios → pedido → merchant → courier → Entregado.
3. **Completar módulos:** catálogo merchant, moderación admin, mandados, front por rol.
4. **Cierre:** tests de invariantes críticas, seed de datos, deploy, documentación y video.

### 4.2 Priorización

- **P0 (obligatorio en demo):** auth, aprobación admin, catálogo, carrito mono-comercio, pedidos + estados, panel courier.
- **P1 (si el tiempo alcanza):** mandados, UX de reemplazo de carrito, búsqueda básica.
- **P2 (futuro / fuera de MVP):** pagos, GPS, push, chat, ratings.

---

## 5. Organización del repositorio

```
UrbanDelivery/
├── README.md
├── docs/                 # Propuesta, luego ER y módulos (2.ª entrega)
├── backend/              # API Spring Boot
└── frontend/             # App React
```

---

## 6. Solicitud al tutor

Se solicita a **Londero, Oscar** la evaluación de:

1. **Viabilidad** del problema y de la solución propuesta.
2. **Alcance** del MVP (incluido el fuera de alcance).
3. **Plazos** respecto a las entregas del 30/08, 27/09 y 14/11.

Con su conformidad, el equipo presentará la propuesta al comité de trabajo final.
