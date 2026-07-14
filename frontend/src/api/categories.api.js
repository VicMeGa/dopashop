const BASE = '/api/store'

export async function fetchCategories() {
  const res = await fetch(`${BASE}/categories`)
  if (!res.ok) throw new Error('Failed to fetch categories')
  return res.json()
}
