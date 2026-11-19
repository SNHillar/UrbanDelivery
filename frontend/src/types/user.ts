import type { Role } from "./rol"

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

/** Alta propia. `merchant` solo se manda cuando `role` es MERCHANT. */
export interface RegisterRequest {
  email: string
  password: string
  fullName: string
  phone?: string
  role: 'CLIENT' | 'MERCHANT'
  merchant?: {
    name: string
    category?: string
    address: string
    phone?: string
    openingHours?: string
  }
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

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** Comercio visto por el ADMIN: incluye estado de aprobación y datos del dueño. */
export interface AdminMerchant extends Merchant {
  approvalStatus: ApprovalStatus
  ownerFullName: string
  ownerEmail: string
}





