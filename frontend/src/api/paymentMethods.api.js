import { apiFetch } from './client'

const BASE = '/api/payment-methods'

export async function getPaymentMethods() {
  const res = await apiFetch(BASE)
  if (!res.ok) throw new Error('Error al cargar métodos de pago')
  return res.json()
}

export async function createPaymentMethod(data) {
  const res = await apiFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al crear método de pago')
  }
  return res.json()
}

export async function deletePaymentMethod(id) {
  const res = await apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Error al eliminar método de pago')
}
