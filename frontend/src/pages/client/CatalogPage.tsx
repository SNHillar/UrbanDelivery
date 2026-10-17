import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { api } from '../../api/client'
import { formatMoney } from '../../api/format'
import type { Merchant, OrderDetail, Product } from '../../api/types'
import { Button, Card, ErrorMessage } from '../../components/ui'

/** Catálogo + carrito. El carrito vive solo en el front y es de un único comercio (invariante #4). */
export function CatalogPage() {
  const merchantId = Number(useParams().merchantId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [cart, setCart] = useState<Record<number, number>>({})
  const [notes, setNotes] = useState('')

  const merchant = useQuery({ queryKey: ['merchant', merchantId], queryFn: () => api<Merchant>(`/merchants/${merchantId}`) })
  const products = useQuery({
    queryKey: ['merchant', merchantId, 'products'],
    queryFn: () => api<Product[]>(`/merchants/${merchantId}/products`),
  })

  const placeOrder = useMutation({
    mutationFn: () =>
      api<OrderDetail>('/orders', {
        method: 'POST',
        body: {
          merchantId,
          fulfillmentType: 'PICKUP',
          notes,
          items: Object.entries(cart).map(([productId, quantity]) => ({ productId: Number(productId), quantity })),
        },
      }),
    onSuccess: (detail) => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      navigate(`/mis-pedidos/${detail.order.id}`)
    },
  })

  const change = (productId: number, delta: number) =>
    setCart((current) => {
      const quantity = (current[productId] ?? 0) + delta
      const next = { ...current }
      if (quantity <= 0) delete next[productId]
      else next[productId] = quantity
      return next
    })

  const lines = (products.data ?? []).filter((p) => cart[p.id])
  const total = lines.reduce((sum, p) => sum + p.price * cart[p.id], 0)

  return (
    <section className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold">{merchant.data?.name ?? '…'}</h1>
        <p className="text-sm text-stone-600">{merchant.data?.address} · Retiro en el local</p>
      </div>
      <ErrorMessage error={merchant.error ?? products.error} />

      <div className="space-y-2">
        {products.data?.map((product) => (
          <Card key={product.id}>
            <div className="flex items-center justify-between gap-3">
              <div className={product.available ? '' : 'opacity-50'}>
                <h2 className="font-medium">{product.name}</h2>
                {product.description && <p className="text-xs text-stone-500">{product.description}</p>}
                <p className="text-sm tabular-nums">{formatMoney(product.price)}</p>
              </div>
              {product.available ? (
                <div className="flex items-center gap-2">
                  {cart[product.id] && (
                    <>
                      <Button variant="secondary" onClick={() => change(product.id, -1)} aria-label="Quitar uno">−</Button>
                      <span className="w-5 text-center tabular-nums">{cart[product.id]}</span>
                    </>
                  )}
                  <Button variant="secondary" onClick={() => change(product.id, 1)} aria-label="Agregar uno">+</Button>
                </div>
              ) : (
                <span className="text-xs font-medium text-stone-500">No disponible</span>
              )}
            </div>
          </Card>
        ))}
      </div>

      {lines.length > 0 && (
        <Card>
          <h2 className="mb-2 font-semibold">Tu pedido</h2>
          <ul className="text-sm">
            {lines.map((p) => (
              <li key={p.id} className="flex justify-between">
                <span>{cart[p.id]} × {p.name}</span>
                <span className="tabular-nums">{formatMoney(p.price * cart[p.id])}</span>
              </li>
            ))}
          </ul>
          <p className="mt-2 flex justify-between border-t border-stone-200 pt-2 font-semibold">
            <span>Total</span>
            <span className="tabular-nums">{formatMoney(total)}</span>
          </p>
          <p className="mt-1 text-xs text-stone-500">El pago se arregla con el comercio al retirar. UrbanDelivery no cobra los productos.</p>
          <textarea value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={500} rows={2}
            placeholder="Notas para el comercio (opcional)"
            className="mt-3 w-full rounded-lg border border-stone-300 px-3 py-2 text-sm" />
          <ErrorMessage error={placeOrder.error} />
          <Button className="mt-2 w-full" disabled={placeOrder.isPending} onClick={() => placeOrder.mutate()}>
            {placeOrder.isPending ? 'Enviando…' : 'Hacer pedido para retirar'}
          </Button>
        </Card>
      )}
    </section>
  )
}
