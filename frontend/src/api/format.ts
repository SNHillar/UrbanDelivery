const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 })
const time = new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' })

export const formatMoney = (value: number) => money.format(value)
export const formatDateTime = (iso: string) => time.format(new Date(iso))
