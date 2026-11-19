import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useParams } from 'react-router'
import { api } from '../../api/client'
import { formatDateTime } from '../../api/format'
import type { OrderDetail } from '../../types/order'
import { OrderItems } from '../../components/OrderItems'
import { Button, Card, ErrorMessage, StatusBadge } from '../../components/ui'

export function OrderDetailPage() {
  const orderId = Number(useParams().orderId)
  const queryClient = useQueryClient()
  const { data, error } = useQuery({
    queryKey: ['orders', orderId],
    queryFn: () => api<OrderDetail>(`/orders/${orderId}`),
    // Sin WebSockets en P0: el cliente se entera de los cambios por polling
    refetchInterval: (query) => (query.state.data && isFinal(query.state.data) ? false : 5_000),
  })

  const cancel = useMutation({
    mutationFn: () => api<OrderDetail>(`/orders/${orderId}/cancel`, { method: 'POST' }),
    onSuccess: (detail) => {
      queryClient.setQueryData(['orders', orderId], detail)
      queryClient.invalidateQueries({ queryKey: ['orders', 'mine'] })
    },
  })

  if (error) return <ErrorMessage error={error} />
  if (!data) return <p className="text-stone-500">Cargando…</p>
  const { order, history } = data

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight">Pedido #{order.id}</h1>
        <StatusBadge status={order.status} label={order.statusLabel} />
      </div>

      {order.status === 'READY' && (
        <p className="rounded-xl bg-emerald-50 p-4 text-emerald-900">
          ¡Listo! Podés retirarlo en <strong>{order.merchantName}</strong>, {order.merchantAddress}.
        </p>
      )}
      {order.statusReason && <p className="rounded-xl bg-red-50 p-4 text-sm text-red-800">Motivo: {order.statusReason}</p>}

      <Card>
        <h2 className="mb-3 text-lg font-bold">{order.merchantName}</h2>
        <OrderItems order={order} />
      </Card>

      <Card>
        <h2 className="mb-3 text-lg font-bold">Seguimiento</h2>
        <ol className="space-y-2 text-sm">
          {history.map((change, index) => (
            <li key={index} className="flex justify-between gap-3">
              <span>
                {change.toStatus}
                <span className="text-stone-500"> · {change.actorName}</span>
                {change.reason && <span className="text-stone-500"> · {change.reason}</span>}
              </span>
              <span className="shrink-0 text-stone-500">{formatDateTime(change.at)}</span>
            </li>
          ))}
        </ol>
      </Card>

      {order.status === 'PENDING' && (
        <>
          <ErrorMessage error={cancel.error} />
          <Button variant="danger" disabled={cancel.isPending} onClick={() => cancel.mutate()}>
            Cancelar pedido
          </Button>
        </>
      )}
    </section>
  )
}

function isFinal({ order }: OrderDetail) {
  return order.status === 'COMPLETED' || order.status === 'REJECTED' || order.status === 'CANCELLED'
}
