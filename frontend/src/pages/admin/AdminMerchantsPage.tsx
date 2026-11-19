import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../api/client'
import type { AdminMerchant } from '../../types/user'
import { Button, Card, ErrorMessage } from '../../components/ui'

type Action = 'approve' | 'reject'

/** Moderación (módulo "admin"): altas de comercio pendientes de aprobación. */
export function AdminMerchantsPage() {
  const queryClient = useQueryClient()

  const pending = useQuery({
    queryKey: ['admin', 'merchants', 'pending'],
    queryFn: () => api<AdminMerchant[]>('/admin/merchants?status=PENDING'),
    refetchInterval: 15_000,
  })

  const act = useMutation({
    mutationFn: ({ merchantId, action }: { merchantId: number; action: Action }) =>
      api<AdminMerchant>(`/admin/merchants/${merchantId}/${action}`, { method: 'POST' }),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['admin', 'merchants'] }),
  })

  return (
    <section className="space-y-6">
      <h1 className="text-3xl font-extrabold tracking-tight">Comercios pendientes</h1>
      <ErrorMessage error={pending.error ?? act.error} />

      {pending.data?.length === 0 && (
        <p className="text-sm text-stone-500">No hay altas de comercio esperando aprobación.</p>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {pending.data?.map((merchant) => (
          <Card key={merchant.id}>
            <div className="mb-4">
              <h2 className="text-lg font-bold">{merchant.name}</h2>
              <p className="mt-1 text-sm text-stone-600">{merchant.category} · {merchant.address}</p>
              <p className="mt-1 text-xs text-stone-500">Dueño: {merchant.ownerFullName} · {merchant.ownerEmail}</p>
            </div>
            <div className="flex gap-2">
              <Button disabled={act.isPending} onClick={() => act.mutate({ merchantId: merchant.id, action: 'approve' })}>
                Aprobar
              </Button>
              <Button variant="danger" disabled={act.isPending}
                onClick={() => act.mutate({ merchantId: merchant.id, action: 'reject' })}>
                Rechazar
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </section>
  )
}
