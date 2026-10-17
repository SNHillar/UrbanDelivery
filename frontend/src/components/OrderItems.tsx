import { formatMoney } from '../api/format'
import type { Order } from '../api/types'

export function OrderItems({ order }: { order: Order }) {
  return (
    <div className="text-sm">
      <ul className="divide-y divide-stone-100">
        {order.items.map((item) => (
          <li key={item.productId} className="flex justify-between py-1">
            <span>{item.quantity} × {item.productName}</span>
            <span className="tabular-nums">{formatMoney(item.lineTotal)}</span>
          </li>
        ))}
      </ul>
      <div className="mt-1 flex justify-between border-t border-stone-200 pt-1 font-semibold">
        <span>Total</span>
        <span className="tabular-nums">{formatMoney(order.total)}</span>
      </div>
      {order.notes && <p className="mt-2 text-stone-600">Nota: {order.notes}</p>}
    </div>
  )
}
