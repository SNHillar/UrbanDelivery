import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { api } from '../../api/client'
import { formatDateTime } from '../../api/format'
import type { Order, OrderDetail, OrderStatus } from '../../api/types'
import { OrderItems } from '../../components/OrderItems'
import { Button, Card, ErrorMessage, StatusBadge } from '../../components/ui'

const ACTIVE: OrderStatus[] = ['PENDING', 'PREPARING', 'READY']

const SECTIONS: { status: OrderStatus; title: string }[] = [
  { status: 'PENDING', title: 'Nuevos' },
  { status: 'PREPARING', title: 'En preparación' },
  { status: 'READY', title: 'Listos para retirar' },
]

type Action = 'accept' | 'reject' | 'ready' | 'complete' | 'cancel'

/** Bandeja del comercio: pedidos activos, los más viejos primero. */
export function MerchantOrdersPage() {
  const queryClient = useQueryClient()
  const [showHistory, setShowHistory] = useState(false)

  const active = useQuery({
    queryKey: ['orders', 'merchant', 'active'],
    queryFn: () => api<Order[]>(`/merchant/orders?${ACTIVE.map((s) => `status=${s}`).join('&')}`),
    refetchInterval: 10_000,
  })
  const all = useQuery({
    queryKey: ['orders', 'merchant', 'all'],
    queryFn: () => api<Order[]>('/merchant/orders'),
    enabled: showHistory,
  })

  const act = useMutation({
    mutationFn: ({ orderId, action, reason }: { orderId: number; action: Action; reason?: string }) =>
      api<OrderDetail>(`/orders/${orderId}/${action}`, {
        method: 'POST',
        body: reason === undefined ? undefined : { reason },
      }),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['orders', 'merchant'] }),
  })

  const run = (orderId: number, action: Action) => {
    if (action === 'reject' || action === 'cancel') {
      const reason = window.prompt(action === 'reject' ? 'Motivo del rechazo' : 'Motivo de la cancelación')
      if (!reason?.trim()) return
      act.mutate({ orderId, action, reason })
    } else {
      act.mutate({ orderId, action })
    }
  }

  return (
    <section className="space-y-5">
      <h1 className="text-2xl font-bold">Pedidos</h1>
      <ErrorMessage error={active.error ?? act.error} />

      {SECTIONS.map(({ status, title }) => {
        const orders = active.data?.filter((o) => o.status === status) ?? []
        return (
          <div key={status} className="space-y-2">
            <h2 className="font-semibold text-stone-700">{title} ({orders.length})</h2>
            {orders.map((order) => (
              <Card key={order.id}>
                <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <span className="font-semibold">#{order.id} · {order.clientName}</span>
                    {order.clientPhone && <span className="text-sm text-stone-500"> · {order.clientPhone}</span>}
                  </div>
                  <span className="text-xs text-stone-500">{formatDateTime(order.createdAt)}</span>
                </div>
                <OrderItems order={order} />
                <div className="mt-3 flex flex-wrap gap-2">
                  {status === 'PENDING' && (
                    <>
                      <Button disabled={act.isPending} onClick={() => run(order.id, 'accept')}>Aceptar</Button>
                      <Button variant="danger" disabled={act.isPending} onClick={() => run(order.id, 'reject')}>Rechazar</Button>
                    </>
                  )}
                  {status === 'PREPARING' && (
                    <Button disabled={act.isPending} onClick={() => run(order.id, 'ready')}>Marcar listo</Button>
                  )}
                  {status === 'READY' && (
                    <Button disabled={act.isPending} onClick={() => run(order.id, 'complete')}>Cliente retiró</Button>
                  )}
                  {status !== 'PENDING' && (
                    <Button variant="danger" disabled={act.isPending} onClick={() => run(order.id, 'cancel')}>Cancelar</Button>
                  )}
                </div>
              </Card>
            ))}
          </div>
        )
      })}

      <div>
        <Button variant="secondary" onClick={() => setShowHistory((v) => !v)}>
          {showHistory ? 'Ocultar historial' : 'Ver todos los pedidos'}
        </Button>
        {showHistory && (
          <ul className="mt-3 space-y-1 text-sm">
            {all.data?.map((order) => (
              <li key={order.id} className="flex items-center justify-between gap-2 rounded-lg bg-white px-3 py-2">
                <span>#{order.id} · {order.clientName} · {formatDateTime(order.createdAt)}</span>
                <StatusBadge status={order.status} label={order.status} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}
