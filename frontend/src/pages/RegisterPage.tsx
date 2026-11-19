import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { homeFor } from '../auth/home'
import { Button, Card, ErrorMessage } from '../components/ui'
import type { RegisterRequest } from '../types/user'

type AccountType = 'CLIENT' | 'MERCHANT'

export function RegisterPage() {
  const { user, register } = useAuth()
  const navigate = useNavigate()
  const [accountType, setAccountType] = useState<AccountType>('CLIENT')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [phone, setPhone] = useState('')
  const [merchantName, setMerchantName] = useState('')
  const [merchantCategory, setMerchantCategory] = useState('')
  const [merchantAddress, setMerchantAddress] = useState('')
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to={homeFor(user.role)} replace />

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const request: RegisterRequest = {
        email, password, fullName, phone: phone || undefined, role: accountType,
        merchant: accountType === 'MERCHANT'
          ? { name: merchantName, category: merchantCategory || undefined, address: merchantAddress }
          : undefined,
      }
      const registered = await register(request)
      navigate(homeFor(registered.role), { replace: true })
    } catch (e) {
      setError(e)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto mt-6 max-w-md sm:mt-16">
      <Card>
        <h1 className="mb-6 text-2xl font-extrabold tracking-tight">Crear cuenta</h1>

        <div className="mb-5 flex gap-2">
          <Button type="button" variant={accountType === 'CLIENT' ? 'primary' : 'secondary'}
            className="flex-1" onClick={() => setAccountType('CLIENT')}>
            Soy cliente
          </Button>
          <Button type="button" variant={accountType === 'MERCHANT' ? 'primary' : 'secondary'}
            className="flex-1" onClick={() => setAccountType('MERCHANT')}>
            Soy un comercio
          </Button>
        </div>

        <form onSubmit={submit} className="space-y-4">
          <label className="block text-sm font-medium">
            Email
            <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
          </label>
          <label className="block text-sm font-medium">
            Contraseña
            <input type="password" required minLength={8} value={password} onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
            <span className="mt-1 block text-xs font-normal text-stone-500">Mínimo 8 caracteres.</span>
          </label>
          <label className="block text-sm font-medium">
            {accountType === 'MERCHANT' ? 'Nombre del dueño' : 'Nombre completo'}
            <input required value={fullName} onChange={(e) => setFullName(e.target.value)}
              className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
          </label>
          <label className="block text-sm font-medium">
            Teléfono (opcional)
            <input value={phone} onChange={(e) => setPhone(e.target.value)}
              className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
          </label>

          {accountType === 'MERCHANT' && (
            <>
              <div className="border-t border-stone-200 pt-4 text-sm font-semibold text-stone-700">Datos del comercio</div>
              <label className="block text-sm font-medium">
                Nombre del comercio
                <input required value={merchantName} onChange={(e) => setMerchantName(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
              </label>
              <label className="block text-sm font-medium">
                Rubro (opcional)
                <input value={merchantCategory} onChange={(e) => setMerchantCategory(e.target.value)}
                  placeholder="Farmacia, rotisería, almacén…"
                  className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
              </label>
              <label className="block text-sm font-medium">
                Dirección de retiro
                <input required value={merchantAddress} onChange={(e) => setMerchantAddress(e.target.value)}
                  className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
              </label>
              <p className="text-xs text-stone-500">
                Tu comercio queda pendiente de aprobación por un administrador antes de aparecer en el catálogo.
                Por ahora solo se puede operar con retiro en el local.
              </p>
            </>
          )}

          <ErrorMessage error={error} />
          <Button type="submit" disabled={submitting} className="w-full py-3 text-base">
            {submitting ? 'Creando cuenta…' : 'Crear cuenta'}
          </Button>
        </form>
      </Card>
    </div>
  )
}
