import type { Role } from '../types/rol'

export function homeFor(role: Role) {
  if (role === 'MERCHANT') return '/comercio/pedidos'
  if (role === 'ADMIN') return '/admin/comercios'
  return '/comercios'
}
