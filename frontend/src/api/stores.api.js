import { apiFetch } from './client'

const BASE = '/api/stores'

export async function getStoreById(id) {
  const res = await apiFetch(`${BASE}/${id}`)
  if (!res.ok) {
    if (res.status === 404) return null
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al cargar tienda')
  }
  return res.json()
}

export async function getStoreProducts(id) {
  const res = await apiFetch(`${BASE}/${id}/products`)
  if (!res.ok) {
    if (res.status === 404) return []
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al cargar productos de la tienda')
  }
  return res.json()
}
