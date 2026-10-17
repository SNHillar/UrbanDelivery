export type Role = 'CLIENT' | 'MERCHANT' | 'COURIER' | 'ADMIN'

export type OrderStatus = 'PENDING' | 'PREPARING' | 'READY' | 'COMPLETED' | 'REJECTED' | 'CANCELLED'

export interface UserInfo {
  id: number
  email: string
  fullName: string
  role: Role
  merchantId: number | null
}

export interface LoginResponse {
  token: string
  expiresAt: string
  user: UserInfo
}

export interface Merchant {
  id: number
  name: string
  category: string | null
  address: string
  phone: string | null
  openingHours: string | null
  offersPickup: boolean
  offersDelivery: boolean
}

export interface Product {
  id: number
  merchantId: number
  name: string
  description: string | null
  price: number
  available: boolean
}

export interface OrderItem {
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export interface Order {
  id: number
  merchantId: number
  merchantName: string
  merchantAddress: string
  clientName: string
  clientPhone: string | null
  fulfillmentType: 'PICKUP' | 'DELIVERY'
  status: OrderStatus
  statusLabel: string
  statusReason: string | null
  notes: string | null
  subtotal: number
  deliveryFee: number
  total: number
  createdAt: string
  acceptedAt: string | null
  readyAt: string | null
  completedAt: string | null
  items: OrderItem[]
}

export interface StatusChange {
  entity: 'ORDER' | 'DELIVERY'
  fromStatus: string | null
  toStatus: string
  actorName: string
  actorRole: Role | null
  reason: string | null
  at: string
}

export interface OrderDetail {
  order: Order
  history: StatusChange[]
}
