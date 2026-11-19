import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { api } from '../../api/client'
import type { Merchant } from '../../types/user'
import { Card, ErrorMessage } from '../../components/ui'

export function MerchantsPage() {
  const { data: merchants, isPending, error } = useQuery({
    queryKey: ['merchants'],
    queryFn: () => api<Merchant[]>('/merchants'),
  })

  return (
    <section className="space-y-6">
      <h1 className="text-3xl font-extrabold tracking-tight">Comercios</h1>
      {isPending && <p className="text-stone-500">Cargando…</p>}
      <ErrorMessage error={error} />
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {merchants?.map((merchant) => (
          <Link key={merchant.id} to={`/comercios/${merchant.id}`} className="block">
            <Card interactive>
              <div className="flex items-baseline justify-between gap-2">
                <h2 className="text-lg font-bold">{merchant.name}</h2>
                <span className="text-xs font-medium text-emerald-700">{merchant.category}</span>
              </div>
              <p className="mt-1 text-sm text-stone-600">{merchant.address}</p>
              {merchant.openingHours && <p className="mt-1 text-xs text-stone-500">{merchant.openingHours}</p>}
            </Card>
          </Link>
        ))}
      </div>
    </section>
  )
}
