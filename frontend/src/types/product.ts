export interface Product {
    id: number
    merchantId: number
    name: string
    description: string | null
    price: number
    available: boolean
  }