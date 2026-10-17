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





