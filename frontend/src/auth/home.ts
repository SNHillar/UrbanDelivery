import type { Role } from '../types/rol'

export function homeFor(role: Role) {
  return role === 'MERCHANT' ? '/comercio/pedidos' : '/comercios'
}
