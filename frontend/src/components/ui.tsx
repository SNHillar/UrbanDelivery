import type { ButtonHTMLAttributes, ReactNode } from 'react'
import type { OrderStatus } from '../api/types'

const statusColors: Record<OrderStatus, string> = {
  PENDING: 'bg-amber-100 text-amber-800',
  PREPARING: 'bg-sky-100 text-sky-800',
  READY: 'bg-emerald-100 text-emerald-800',
  COMPLETED: 'bg-stone-200 text-stone-700',
  REJECTED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

export function StatusBadge({ status, label }: { status: OrderStatus; label: string }) {
  return <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${statusColors[status]}`}>{label}</span>
}

export function Card({ children }: { children: ReactNode }) {
  return <div className="rounded-xl border border-stone-200 bg-white p-4 shadow-sm">{children}</div>
}

type Variant = 'primary' | 'secondary' | 'danger'

const variants: Record<Variant, string> = {
  primary: 'bg-emerald-700 text-white hover:bg-emerald-800',
  secondary: 'border border-stone-300 bg-white text-stone-800 hover:bg-stone-100',
  danger: 'border border-red-300 bg-white text-red-700 hover:bg-red-50',
}

export function Button({ variant = 'primary', className = '', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }) {
  return (
    <button
      {...props}
      className={`rounded-lg px-3 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${className}`}
    />
  )
}

export function ErrorMessage({ error }: { error: unknown }) {
  if (!error) return null
  return <p className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error instanceof Error ? error.message : 'Error inesperado'}</p>
}
