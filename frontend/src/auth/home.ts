import type { Role } from '../api/types'

export function homeFor(role: Role) {
  return role === 'MERCHANT' ? '/comercio/pedidos' : '/comercios'
}
