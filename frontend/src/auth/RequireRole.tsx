import type { ReactNode } from 'react'
import { Navigate } from 'react-router'
import type { Role } from '../api/types'
import { useAuth } from './AuthContext'
import { homeFor } from './home'

/** Ruta protegida: sin sesión va al login; con otro rol, a su pantalla de inicio. */
export function RequireRole({ role, children }: { role: Role; children: ReactNode }) {
  const { user, loading } = useAuth()
  if (loading) return <p className="p-6 text-stone-500">Cargando…</p>
  if (!user) return <Navigate to="/login" replace />
  if (user.role !== role) return <Navigate to={homeFor(user.role)} replace />
  return children
}
