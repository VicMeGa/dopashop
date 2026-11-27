import { apiFetch } from './client'

const BASE = '/api/products'

export async function createReview(productId, data) {
  const res = await apiFetch(`${BASE}/${productId}/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    const error = new Error(err.message || err.error || 'Error al enviar reseña')
    error.status = res.status
    throw error
  }
  return res.json()
}

export async function getReviewsForProduct(productId) {
  const res = await fetch(`${BASE}/${productId}/reviews`)
  if (!res.ok) throw new Error('Error al cargar reseñas')
  return res.json()
}
