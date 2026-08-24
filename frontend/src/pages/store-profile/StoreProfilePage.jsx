import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getStoreById, getStoreProducts } from '../../api/stores.api'
import ProductCard from '../../components/ProductCard'

export default function StoreProfilePage({ onSelect, onAddToCart }) {
  const { id } = useParams()
  const [store, setStore] = useState(undefined)
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')

    Promise.all([getStoreById(id), getStoreProducts(id)])
      .then(([storeData, productsData]) => {
        if (cancelled) return
        setStore(storeData)
        setProducts(productsData)
      })
      .catch(err => {
        if (cancelled) return
        setError(err.message || 'Error al cargar la tienda')
        setStore(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [id])

  if (loading) {
    return (
      <div className="store-body">
        <main className="store-main">
          <p className="store-loading">Cargando tienda...</p>
        </main>
      </div>
    )
  }

  if (!store) {
    return (
      <div className="store-body">
        <main className="store-main">
          <div className="page-header">
            <h2>Tienda no encontrada</h2>
          </div>
          {error && <p className="auth-error">{error}</p>}
          <p className="empty-state">La tienda que buscas no existe o fue eliminada.</p>
          <Link to="/" className="btn-primary" style={{ display: 'inline-block', marginTop: 16, textDecoration: 'none' }}>
            Volver al catálogo
          </Link>
        </main>
      </div>
    )
  }

  return (
    <div className="store-body">
      <main className="store-main">
        <div className="page-header" style={{ flexDirection: 'column', alignItems: 'flex-start', gap: '8px' }}>
          <h2>{store.name}</h2>
          {store.description && <p className="my-store-description" style={{ margin: 0 }}>{store.description}</p>}
        </div>

        <section>
          <h3>Productos de la tienda ({products.length})</h3>
          {products.length === 0 ? (
            <p className="empty-state">Esta tienda aún no tiene productos publicados.</p>
          ) : (
            <div className="product-grid">
              {products.map(p => (
                <ProductCard key={p.id} product={p} onSelect={onSelect} onAddToCart={onAddToCart} />
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  )
}
