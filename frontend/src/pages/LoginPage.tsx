import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { homeFor } from '../auth/home'
import { Button, Card, ErrorMessage } from '../components/ui'

const DEMO_USERS = [
  { email: 'cliente@urbandelivery.local', label: 'Cliente' },
  { email: 'rotiseria@urbandelivery.local', label: 'Rotisería' },
  { email: 'farmacia@urbandelivery.local', label: 'Farmacia' },
  { email: 'admin@urbandelivery.local', label: 'Admin' },
]

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) return <Navigate to={homeFor(user.role)} replace />

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const logged = await login(email, password)
      navigate(homeFor(logged.role), { replace: true })
    } catch (e) {
      setError(e)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto mt-6 max-w-md sm:mt-16">
      <Card>
        <h1 className="mb-6 text-2xl font-extrabold tracking-tight">Ingresar</h1>
        <form onSubmit={submit} className="space-y-4">
          <label className="block text-sm font-medium">
            Email
            <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
          </label>
          <label className="block text-sm font-medium">
            Contraseña
            <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-xl border border-stone-300 px-4 py-2.5" />
          </label>
          <ErrorMessage error={error} />
          <Button type="submit" disabled={submitting} className="w-full py-3 text-base">
            {submitting ? 'Ingresando…' : 'Ingresar'}
          </Button>
        </form>
        <p className="mt-4 text-center text-sm text-stone-600">
          ¿No tenés cuenta? <Link to="/registro" className="font-semibold text-emerald-700 hover:underline">Registrate</Link>
        </p>
        <div className="mt-6 border-t border-stone-200 pt-5 text-sm text-stone-600">
          <p className="mb-3">Usuarios de demostración (contraseña <code>demo1234</code>):</p>
          <div className="flex flex-wrap gap-2">
            {DEMO_USERS.map((demo) => (
              <Button key={demo.email} variant="secondary" type="button"
                onClick={() => { setEmail(demo.email); setPassword('demo1234') }}>
                {demo.label}
              </Button>
            ))}
          </div>
        </div>
      </Card>
    </div>
  )
}
