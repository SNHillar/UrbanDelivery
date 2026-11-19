import { NavLink, Outlet } from 'react-router'
import { useAuth } from '../auth/AuthContext'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-xl px-4 py-2 text-sm font-semibold transition-colors ${isActive ? 'bg-emerald-700 text-white shadow-sm' : 'text-stone-700 hover:bg-stone-200'}`

export function Layout() {
  const { user, logout } = useAuth()
  return (
    <div className="min-h-screen bg-stone-50">
      <header className="border-b border-stone-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-6 py-5">
          <span className="text-2xl font-extrabold tracking-tight text-emerald-800">UrbanDelivery</span>
          {user && (
            <nav className="flex flex-wrap items-center gap-2">
              {user.role === 'CLIENT' && (
                <>
                  <NavLink to="/comercios" className={linkClass}>Comercios</NavLink>
                  <NavLink to="/mis-pedidos" className={linkClass}>Mis pedidos</NavLink>
                </>
              )}
              {user.role === 'MERCHANT' && (
                <>
                  <NavLink to="/comercio/pedidos" className={linkClass}>Pedidos</NavLink>
                  <NavLink to="/comercio/productos" className={linkClass}>Productos</NavLink>
                </>
              )}
              {user.role === 'ADMIN' && (
                <NavLink to="/admin/comercios" className={linkClass}>Comercios pendientes</NavLink>
              )}
              <span className="ml-2 hidden text-sm font-medium text-stone-500 sm:inline">{user.fullName}</span>
              <button onClick={logout} className="rounded-xl px-4 py-2 text-sm font-semibold text-stone-600 transition-colors hover:bg-stone-200">
                Salir
              </button>
            </nav>
          )}
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  )
}
