import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../api/client'
import { formatMoney } from '../../api/format'
import type { Product } from '../../types/product'
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
    <section className="space-y-6">
      <h1 className="text-3xl font-extrabold tracking-tight">Productos</h1>
      <ErrorMessage error={products.error ?? toggle.error} />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {products.data?.map((product) => (
          <Card key={product.id}>
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-base font-bold">{product.name}</h2>
                <p className="mt-1 text-sm tabular-nums font-medium text-stone-600">{formatMoney(product.price)}</p>
              </div>
              <label className="flex cursor-pointer items-center gap-2 text-sm font-medium">
                <input type="checkbox" checked={product.available} disabled={toggle.isPending}
                  onChange={() => toggle.mutate(product)} className="h-5 w-5 accent-emerald-700" />
                {product.available ? 'Disponible' : 'No disponible'}
              </label>
            </div>
          </Card>
        ))}
      </div>
    </section>
  )
}
