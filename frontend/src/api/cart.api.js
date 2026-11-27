import { apiFetch } from './client'

const BASE = '/api/cart'

async function parseCart(res, fallback) {
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || fallback)
  }
  return res.json()
}

export async function getCart() {
  const res = await apiFetch(BASE)
  return parseCart(res, 'Error al cargar el carrito')
}

export async function addItem(data) {
  const res = await apiFetch(`${BASE}/items`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return parseCart(res, 'Error al agregar al carrito')
}

export async function updateItem(productId, quantity) {
  const res = await apiFetch(`${BASE}/items/${productId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ quantity }),
  })
  return parseCart(res, 'Error al actualizar el carrito')
}

export async function removeItem(productId) {
  const res = await apiFetch(`${BASE}/items/${productId}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Error al quitar el producto del carrito')
}

export async function clearCart() {
  const res = await apiFetch(BASE, { method: 'DELETE' })
  if (!res.ok) throw new Error('Error al vaciar el carrito')
}

export async function mergeCart(items) {
  const res = await apiFetch(`${BASE}/merge`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ items }),
  })
  return parseCart(res, 'Error al sincronizar el carrito')
}
