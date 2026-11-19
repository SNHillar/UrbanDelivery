# UrbanDelivery — Deploy (API + DB en Render, front en Vercel)

Guía paso a paso para dejar el sistema disponible en línea, tal como pide la entrega final. No hace falta tocar código: toda la configuración ya sale de variables de entorno (ver [backend/README.md](../backend/README.md#configuración) y [frontend/README.md](../frontend/README.md)).

> Los dos servicios se necesitan mutuamente (el front necesita la URL de la API; la API necesita la URL del front para CORS), así que el orden importa: primero la base, después la API, después el front, y por último se vuelve a la API para completar el CORS.

## 1. Base de datos (Render)

1. En [render.com](https://render.com), **New → PostgreSQL**. Plan **Free**. Nombre: `urbandelivery-db`.
2. Cuando esté lista, en la pestaña **Connect** copiar por separado: **Hostname**, **Port** (`5432`), **Database**, **Username**, **Password**. No hace falta la "Internal/External Database URL" completa, porque la API arma su propia URL JDBC con estos datos.

**Importante:** la base Free de Render se borra a los 30 días de creada. Si la defensa oral es mucho después del 21/11, hay que recrearla (o pasar a un plan pago) antes de esa fecha.

## 2. API (Render)

1. **New → Web Service** → conectar el repositorio de GitHub.
2. **Root Directory**: `backend`. Render detecta el `Dockerfile` que está ahí adentro y lo usa para buildear (no hace falta elegir "Docker" a mano en versiones recientes, pero si pregunta el **Environment**, elegirlo).
3. **Instance Type**: Free.
4. **Health Check Path**: `/actuator/health`.
5. Variables de entorno (**Environment → Add Environment Variable**):

   | Variable | Valor |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://<Hostname>:<Port>/<Database>` (con los datos del paso 1) |
   | `DB_USER` | `<Username>` del paso 1 |
   | `DB_PASSWORD` | `<Password>` del paso 1 |
   | `JWT_SECRET` | un secreto propio de al menos 32 caracteres — **no** el de `.env.example`, ese es solo para desarrollo local |
   | `CORS_ALLOWED_ORIGINS` | por ahora `http://localhost:5173` (se corrige en el paso 4, cuando exista la URL de Vercel) |

   `PORT` **no se configura a mano**: Render lo inyecta solo y `application.yml` ya lo lee (`server.port=${PORT:8080}`).
6. Deploy. Al terminar, Render da una URL tipo `https://urbandelivery-api.onrender.com`. Probarla: `https://urbandelivery-api.onrender.com/actuator/health` debería responder `{"status":"UP"}`, y `/api/merchants` debería listar los comercios de demo (Flyway corre las migraciones y carga los datos de `db/demo` solo al arrancar).

**Nota sobre el plan Free:** el servicio se "duerme" después de ~15 minutos sin tráfico y la primera visita después de eso tarda unos 30–50 segundos en responder mientras arranca de nuevo. Es normal, no es un error — avisarlo en el informe/video para que no se confunda con una falla.

## 3. Frontend (Vercel)

1. En [vercel.com](https://vercel.com), **Add New → Project** → importar el mismo repositorio.
2. **Root Directory**: `frontend`. Vercel detecta Vite solo (Framework Preset: Vite); build command y output directory (`dist`) quedan por defecto.
3. Variable de entorno: `VITE_API_URL` = la URL de Render del paso 2, **sin** `/api` al final (ej. `https://urbandelivery-api.onrender.com`). El cliente HTTP del front ya le agrega `/api` a cada pedido.
4. Deploy. Da una URL tipo `https://urbandelivery.vercel.app`.

## 4. Cerrar el CORS

Volver a Render → el Web Service de la API → **Environment** → editar `CORS_ALLOWED_ORIGINS` con la URL real de Vercel del paso 3 (ej. `https://urbandelivery.vercel.app`, sin barra al final). Guardar: Render redeploya solo. Sin este paso el front carga pero los pedidos al API fallan por CORS.

## 5. Verificación final

Abrir la URL de Vercel desde una ventana de incógnito (sin sesión de ningún integrante) y probar el login con los usuarios de demostración (ver [README](../README.md#cómo-levantarlo), contraseña `demo1234` para todos) — exactamente lo que pide el instructivo de la entrega final: que los recursos se puedan abrir "desde una sesión que no pertenezca a ninguno de los integrantes del grupo".

## 6. Qué poner en el README / Informe Final

- Enlace al sistema desplegado: la URL de Vercel.
- Cuenta de demostración: los usuarios de `db/demo` (nunca credenciales reales).
- Aclarar el "cold start" del plan Free si el video lo muestra tardando en cargar.
