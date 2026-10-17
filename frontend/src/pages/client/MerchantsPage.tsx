import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { api } from '../../api/client'
import type { Merchant } from '../../api/types'
import { Card, ErrorMessage } from '../../components/ui'

export function MerchantsPage() {
  const { data: merchants, isPending, error } = useQuery({
    queryKey: ['merchants'],
    queryFn: () => api<Merchant[]>('/merchants'),
  })

  return (
    <section className="space-y-3">
      <h1 className="text-2xl font-bold">Comercios</h1>
      {isPending && <p className="text-stone-500">Cargando…</p>}
      <ErrorMessage error={error} />
      {merchants?.map((merchant) => (
        <Link key={merchant.id} to={`/comercios/${merchant.id}`} className="block">
          <Card>
            <div className="flex items-baseline justify-between gap-2">
              <h2 className="font-semibold">{merchant.name}</h2>
              <span className="text-xs text-stone-500">{merchant.category}</span>
            </div>
            <p className="text-sm text-stone-600">{merchant.address}</p>
            {merchant.openingHours && <p className="text-xs text-stone-500">{merchant.openingHours}</p>}
          </Card>
        </Link>
      ))}
    </section>
  )
}
