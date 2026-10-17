import { NavLink, Outlet } from 'react-router'
import { useAuth } from '../auth/AuthContext'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-1.5 text-sm font-medium ${isActive ? 'bg-emerald-700 text-white' : 'text-stone-700 hover:bg-stone-200'}`

export function Layout() {
  const { user, logout } = useAuth()
  return (
    <div className="mx-auto min-h-screen max-w-3xl px-4 pb-10">
      <header className="flex flex-wrap items-center justify-between gap-3 py-4">
        <span className="text-lg font-bold text-emerald-800">UrbanDelivery</span>
        {user && (
          <nav className="flex flex-wrap items-center gap-1">
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
            <span className="ml-2 hidden text-sm text-stone-500 sm:inline">{user.fullName}</span>
            <button onClick={logout} className="rounded-md px-3 py-1.5 text-sm text-stone-600 hover:bg-stone-200">
              Salir
            </button>
          </nav>
        )}
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
