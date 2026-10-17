import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../api/client'
import { formatMoney } from '../../api/format'
import type { Product } from '../../api/types'
import { Card, ErrorMessage } from '../../components/ui'

/** Disponibilidad con un click: evita recibir pedidos que después hay que rechazar. */
export function MerchantProductsPage() {
  const queryClient = useQueryClient()
  const products = useQuery({ queryKey: ['merchant', 'products'], queryFn: () => api<Product[]>('/merchant/products') })

  const toggle = useMutation({
    mutationFn: (product: Product) =>
      api<Product>(`/merchant/products/${product.id}/availability`, {
        method: 'PATCH',
        body: { available: !product.available },
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['merchant', 'products'] }),
  })

  return (
    <section className="space-y-3">
      <h1 className="text-2xl font-bold">Productos</h1>
      <ErrorMessage error={products.error ?? toggle.error} />
      {products.data?.map((product) => (
        <Card key={product.id}>
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="font-medium">{product.name}</h2>
              <p className="text-sm tabular-nums text-stone-600">{formatMoney(product.price)}</p>
            </div>
            <label className="flex cursor-pointer items-center gap-2 text-sm">
              <input type="checkbox" checked={product.available} disabled={toggle.isPending}
                onChange={() => toggle.mutate(product)} className="h-4 w-4 accent-emerald-700" />
              {product.available ? 'Disponible' : 'No disponible'}
            </label>
          </div>
        </Card>
      ))}
    </section>
  )
}
