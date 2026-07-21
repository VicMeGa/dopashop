import { apiFetch } from './client'

const BASE = '/api/orders'

export async function createOrder(data) {
  const res = await apiFetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al crear orden')
  }
  return res.json()
}

export async function attemptPayment(orderId) {
  const res = await apiFetch(`${BASE}/${orderId}/pay`, { method: 'POST' })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al procesar pago')
  }
  return res.json()
}

export async function getMyOrders() {
  const res = await apiFetch(`${BASE}/mine`)
  if (!res.ok) throw new Error('Error al cargar órdenes')
  return res.json()
}

export async function getOrderById(id) {
  const res = await apiFetch(`${BASE}/${id}`)
  if (!res.ok) throw new Error('Error al cargar orden')
  return res.json()
}
