import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { homeFor } from '../auth/home'
import { Button, Card, ErrorMessage } from '../components/ui'

const DEMO_USERS = [
  { email: 'cliente@urbandelivery.local', label: 'Cliente' },
  { email: 'rotiseria@urbandelivery.local', label: 'Rotisería' },
  { email: 'farmacia@urbandelivery.local', label: 'Farmacia' },
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
    <div className="mx-auto mt-10 max-w-sm">
      <Card>
        <h1 className="mb-4 text-xl font-bold">Ingresar</h1>
        <form onSubmit={submit} className="space-y-3">
          <label className="block text-sm">
            Email
            <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2" />
          </label>
          <label className="block text-sm">
            Contraseña
            <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2" />
          </label>
          <ErrorMessage error={error} />
          <Button type="submit" disabled={submitting} className="w-full">
            {submitting ? 'Ingresando…' : 'Ingresar'}
          </Button>
        </form>
        <div className="mt-5 border-t border-stone-200 pt-4 text-sm text-stone-600">
          <p className="mb-2">Usuarios de demostración (contraseña <code>demo1234</code>):</p>
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
