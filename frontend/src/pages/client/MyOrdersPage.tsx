import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { api } from '../../api/client'
import { formatDateTime, formatMoney } from '../../api/format'
import type { Order } from '../../types/order'
import { Card, ErrorMessage, StatusBadge } from '../../components/ui'

export function MyOrdersPage() {
  const { data: orders, isPending, error } = useQuery({
    queryKey: ['orders', 'mine'],
    queryFn: () => api<Order[]>('/orders/mine'),
    refetchInterval: 10_000,
  })

  return (
    <section className="space-y-3">
      <h1 className="text-2xl font-bold">Mis pedidos</h1>
      {isPending && <p className="text-stone-500">Cargando…</p>}
      <ErrorMessage error={error} />
      {orders?.length === 0 && <p className="text-stone-500">Todavía no hiciste pedidos.</p>}
      {orders?.map((order) => (
        <Link key={order.id} to={`/mis-pedidos/${order.id}`} className="block">
          <Card>
            <div className="flex items-center justify-between gap-2">
              <h2 className="font-semibold">#{order.id} · {order.merchantName}</h2>
              <StatusBadge status={order.status} label={order.statusLabel} />
            </div>
            <p className="text-sm text-stone-600">
              {formatDateTime(order.createdAt)} · {formatMoney(order.total)}
            </p>
          </Card>
        </Link>
      ))}
    </section>
  )
}
