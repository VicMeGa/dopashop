const BASE = '/api/store'

export async function fetchProducts(params = {}) {
  const query = new URLSearchParams()
  if (params.title) query.set('title', params.title)
  if (params.categoryId) query.set('categoryId', params.categoryId)
  if (params.priceMin) query.set('priceMin', params.priceMin)
  if (params.priceMax) query.set('priceMax', params.priceMax)
  const qs = query.toString()
  const res = await fetch(`${BASE}/products${qs ? '?' + qs : ''}`)
  if (!res.ok) throw new Error('Failed to fetch products')
  return res.json()
}

export async function fetchProduct(id) {
  const res = await fetch(`${BASE}/products/${id}`)
  if (!res.ok) throw new Error('Failed to fetch product')
  return res.json()
}

export async function fetchCategories() {
  const res = await fetch(`${BASE}/categories`)
  if (!res.ok) throw new Error('Failed to fetch categories')
  return res.json()
}
