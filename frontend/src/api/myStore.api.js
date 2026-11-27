import { apiFetch } from './client'

const BASE = '/api/stores/mine'

export async function getMyStore() {
  const res = await apiFetch(`${BASE}`)
  if (!res.ok) {
    if (res.status === 404) return null
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al cargar tienda')
  }
  return res.json()
}

export async function createStore(data) {
  const res = await apiFetch('/api/stores', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: data.name, description: data.description }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al crear tienda')
  }
  return res.json()
}

export async function getMyProducts() {
  const res = await apiFetch(`${BASE}/products`)
  if (!res.ok) throw new Error('Error al cargar tus productos')
  return res.json()
}

export async function reactivateProduct(id) {
  const res = await apiFetch(`${BASE}/products/${id}/reactivate`, { method: 'PUT' })
  if (!res.ok && res.status !== 404) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al reactivar el producto')
  }
}

export async function createProduct(data) {
  const res = await apiFetch(`${BASE}/products`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      title: data.title,
      description: data.description,
      price: data.price,
      stock: data.stock,
      categoryId: data.categoryId,
      imageUrls: data.imageUrls,
    }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al crear producto')
  }
  return res.json()
}

export async function updateProduct(id, data) {
  const res = await apiFetch(`${BASE}/products/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      title: data.title,
      description: data.description,
      price: data.price,
      stock: data.stock,
      categoryId: data.categoryId,
      imageUrls: data.imageUrls,
    }),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al actualizar producto')
  }
  return res.json()
}

export async function deleteProduct(id) {
  const res = await apiFetch(`${BASE}/products/${id}`, { method: 'DELETE' })
  if (!res.ok && res.status !== 404) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al eliminar producto')
  }
}

export async function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await apiFetch(`${BASE}/images`, { method: 'POST', body: formData })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || err.error || 'Error al subir imagen')
  }
  return res.json()
}
