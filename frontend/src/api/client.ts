const BASE_URL = import.meta.env.VITE_API_URL ?? ''
const TOKEN_KEY = 'ud.token'

/** Error devuelto por la API (ProblemDetail con `code`). */
export class ApiError extends Error {
  readonly status: number
  readonly code: string | undefined

  constructor(status: number, message: string, code?: string) {
    super(message)
    this.status = status
    this.code = code
  }
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

let onUnauthorized: () => void = () => {}

/** El AuthProvider se registra acá para cerrar la sesión cuando el token vence. */
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

export async function api<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const headers: Record<string, string> = {}
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'

  const response = await fetch(`${BASE_URL}/api${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    if (response.status === 401 && token) onUnauthorized()
    throw new ApiError(response.status, problem?.detail ?? `Error ${response.status}`, problem?.code)
  }
  return response.status === 204 ? (undefined as T) : response.json()
}
