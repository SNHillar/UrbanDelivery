import { useQueryClient } from '@tanstack/react-query'
import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { api, getToken, setToken, setUnauthorizedHandler } from '../api/client'
import type { LoginResponse, UserInfo } from '../api/types'

interface AuthState {
  user: UserInfo | null
  loading: boolean
  login: (email: string, password: string) => Promise<UserInfo>
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(() => getToken() !== null)

  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
    queryClient.clear()
  }, [queryClient])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    // Si quedó un token guardado, lo validamos contra el backend
    if (getToken()) {
      api<UserInfo>('/auth/me')
        .then(setUser)
        .catch(() => setToken(null))
        .finally(() => setLoading(false))
    }
  }, [logout])

  const login = async (email: string, password: string) => {
    const response = await api<LoginResponse>('/auth/login', { method: 'POST', body: { email, password } })
    setToken(response.token)
    setUser(response.user)
    return response.user
  }

  return <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react/only-export-components
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return context
}
