import { createContext, useState, useEffect, useCallback } from 'react'
import * as authApi from '../api/auth.api'
import { setOnUnauthenticated } from '../api/client'

export const AuthContext = createContext(null)

function loadUser() {
  try {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadUser)
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem('accessToken'))
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem('refreshToken'))
  const [loading, setLoading] = useState(false)

  const isAuthenticated = !!user && !!accessToken

  const saveSession = useCallback((data) => {
    setUser(data.user)
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
  }, [])

  const clearSession = useCallback(() => {
    setUser(null)
    setAccessToken(null)
    setRefreshToken(null)
    localStorage.removeItem('user')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }, [])

  useEffect(() => {
    setOnUnauthenticated(() => {
      clearSession()
    })
  }, [clearSession])

  const login = useCallback(async (email, password) => {
    setLoading(true)
    try {
      const data = await authApi.login(email, password)
      saveSession(data)
      return data
    } finally {
      setLoading(false)
    }
  }, [saveSession])

  const register = useCallback(async (email, password, fullName) => {
    setLoading(true)
    try {
      const data = await authApi.register(email, password, fullName)
      saveSession(data)
      return data
    } finally {
      setLoading(false)
    }
  }, [saveSession])

  const logout = useCallback(async () => {
    const rt = refreshToken
    clearSession()
    if (rt) {
      authApi.logout(rt).catch(() => {})
    }
  }, [refreshToken, clearSession])

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
