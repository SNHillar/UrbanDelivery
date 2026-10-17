# UrbanDelivery — Web (frontend)

SPA del recorrido PICKUP: el cliente arma un pedido para retirar y lo sigue; el comercio lo gestiona desde su bandeja. La interfaz es funcional y mobile-first, sin diseño final.

## Stack

| | |
|---|---|
| Build | Vite 8 + TypeScript 6 |
| UI | React 19 + Tailwind CSS 4 (plugin `@tailwindcss/vite`) |
| Datos | TanStack Query 5 (caché, reintentos y polling) |
| Rutas | React Router 8 |
| Lint | oxlint |
| Paquetes | pnpm (`pnpm-lock.yaml` versionado) |

## Cómo correrlo

Requisitos: Node 22 y pnpm. La API tiene que estar corriendo en `http://localhost:8080` (ver [backend/README.md](../backend/README.md)).

```bash
pnpm install
pnpm dev        # http://localhost:5173
```

| Script | Qué hace |
|---|---|
| `pnpm dev` | Servidor de desarrollo con recarga en caliente |
| `pnpm build` | Chequeo de tipos (`tsc -b`) + build de producción en `dist/` |
| `pnpm lint` | oxlint |
| `pnpm preview` | Sirve el build de `dist/` |

En desarrollo, Vite reenvía `/api` a `localhost:8080` (`vite.config.ts`), así que no hay problemas de CORS y `VITE_API_URL` queda vacío. En un deploy con el front y la API en dominios distintos, definir `VITE_API_URL` con la URL de la API (ver [.env.example](.env.example)) y agregar el dominio del front a `CORS_ALLOWED_ORIGINS` en el backend.

## Recorrido para probar

Abrir dos ventanas (una normal y una de incógnito, porque la sesión se guarda en `localStorage`). La pantalla de login tiene botones que completan los usuarios de demostración (contraseña `demo1234`).

1. **Cliente** (`cliente@urbandelivery.local`): Comercios → Rotisería La Esquina → agregar productos → *Hacer pedido para retirar*.
2. **Comercio** (`rotiseria@urbandelivery.local`): el pedido aparece en *Nuevos* (la bandeja se actualiza sola cada 10 s) → *Aceptar* → *Marcar listo*.
3. **Cliente**: en el detalle del pedido el estado pasa a "Listo para retirar" sin recargar.
4. **Comercio**: *Cliente retiró* → el pedido queda `COMPLETED` y el historial muestra las 4 transiciones con quién hizo cada una.

Variantes: rechazar o cancelar (pide motivo), cancelar desde el cliente mientras está pendiente, marcar un producto como no disponible en *Productos* y ver que desaparece del catálogo.

## Estructura

```
src/
├── main.tsx            QueryClient, AuthProvider y BrowserRouter
├── App.tsx             Rutas
├── api/
│   ├── client.ts       fetch con JWT, ApiError (lee `code` del ProblemDetail), manejo de 401
│   └── format.ts       Formato de moneda y fechas (es-AR)
├── types/              Tipos de las respuestas de la API
│   ├── rol.ts          Role
│   ├── user.ts         UserInfo, LoginResponse, Merchant
│   ├── product.ts      Product
│   └── order.ts        OrderStatus, Order, OrderItem, StatusChange, OrderDetail
├── auth/
│   ├── AuthContext.tsx Sesión: login, logout, validación del token guardado con /auth/me
│   ├── RequireRole.tsx Protege rutas por rol
│   └── home.ts         Pantalla inicial según el rol
├── components/         Layout, StatusBadge, Card, Button, ErrorMessage, OrderItems
└── pages/
    ├── LoginPage.tsx
    ├── client/         MerchantsPage, CatalogPage, MyOrdersPage, OrderDetailPage
    └── merchant/       MerchantOrdersPage, MerchantProductsPage
```

## Rutas

| Ruta | Rol | Pantalla |
|---|---|---|
| `/login` | — | Login |
| `/comercios` | CLIENT | Comercios aprobados |
| `/comercios/:merchantId` | CLIENT | Catálogo + carrito (un solo comercio por pedido) |
| `/mis-pedidos` | CLIENT | Pedidos del cliente |
| `/mis-pedidos/:orderId` | CLIENT | Detalle, estado e historial; cancelar mientras está pendiente |
| `/comercio/pedidos` | MERCHANT | Bandeja por estado con las acciones de la máquina de estados |
| `/comercio/productos` | MERCHANT | Disponibilidad de productos |

Cualquier otra ruta redirige a `/login`; después del login cada rol va a su pantalla inicial.

## Decisiones

- **Sesión.** El token se guarda en `localStorage` (`ud.token`). Al abrir la app se valida con `GET /api/auth/me`; si la API responde 401 (token vencido), se cierra la sesión y se limpia la caché de TanStack Query.
- **Actualizaciones por polling, no WebSockets.** Bandeja del comercio y lista de pedidos cada 10 s; detalle del pedido cada 5 s, y deja de consultar cuando el pedido llega a un estado final. Alcanza para el volumen de un pueblo y evita infraestructura extra.
- **El estado lo decide el backend.** El front no calcula transiciones: muestra los botones según el estado actual y, si la API responde 409 (otra persona ya lo cambió), muestra el mensaje y refresca.
- **Etiqueta para el cliente.** El texto ("Esperando al comercio", "Listo para retirar", …) viene de la API (`statusLabel`), para que haya una sola fuente de verdad.
- **Dinero.** El checkout aclara que la app no cobra los productos: el pago se arregla con el comercio.
- **Motivos con `window.prompt`.** Rechazar y cancelar piden el motivo con un prompt del navegador; es provisorio hasta tener el diseño de UI.
