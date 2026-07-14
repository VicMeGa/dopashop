import { refresh } from './auth.api'

async function getAccessToken() {
  return localStorage.getItem('accessToken')
}

async function getRefreshToken() {
  return localStorage.getItem('refreshToken')
}

function setTokens(accessToken, refreshToken) {
  localStorage.setItem('accessToken', accessToken)
  localStorage.setItem('refreshToken', refreshToken)
}

function clearTokens() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
}

let onUnauthenticated = null

export function setOnUnauthenticated(fn) {
  onUnauthenticated = fn
}

export async function apiFetch(url, options = {}) {
  const token = await getAccessToken()
  const headers = { ...options.headers }
  if (token && !url.startsWith('/api/auth/')) {
    headers['Authorization'] = `Bearer ${token}`
  }
  let res = await fetch(url, { ...options, headers })

  if (res.status === 401 && !url.startsWith('/api/auth/')) {
    const refreshToken = await getRefreshToken()
    if (refreshToken) {
      const data = await refresh(refreshToken)
      if (data) {
        setTokens(data.accessToken, data.refreshToken)
        headers['Authorization'] = `Bearer ${data.accessToken}`
        res = await fetch(url, { ...options, headers })
      } else {
        clearTokens()
        if (onUnauthenticated) onUnauthenticated()
        throw new Error('Session expired')
      }
    } else {
      clearTokens()
      if (onUnauthenticated) onUnauthenticated()
      throw new Error('Not authenticated')
    }
  }

  return res
}
