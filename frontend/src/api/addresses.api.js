import { apiFetch } from './client'

const BASE = '/api/addresses'

export async function getAddresses() {
  const res = await apiFetch(BASE)
  if (!res.ok) throw new Error('Error al cargar direcciones')
  return res.json()
}

export async function createAddress(data) {
  const res = await apiFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al crear dirección')
  }
  return res.json()
}

export async function updateAddress(id, data) {
  const res = await apiFetch(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al actualizar dirección')
  }
  return res.json()
}

export async function deleteAddress(id) {
  const res = await apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Error al eliminar dirección')
}
