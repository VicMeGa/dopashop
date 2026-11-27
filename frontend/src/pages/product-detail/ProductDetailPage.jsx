import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { fetchProduct } from '../../api/products.api'
import ProductDetail from './components/ProductDetail'

export default function ProductDetailPage({ onBack, onAddToCart }) {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    fetchProduct(id)
      .then(data => {
        if (!cancelled) setProduct(data)
      })
      .catch(err => {
        if (!cancelled) setError(err.message)
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
          <p className="store-loading">Cargando producto...</p>
        </main>
      </div>
    )
  }

  if (error || !product) {
    return (
      <div className="store-body">
        <main className="store-main">
          <p className="auth-error">{error || 'Producto no encontrado'}</p>
          <button className="back-btn" onClick={onBack}>&larr; Volver</button>
        </main>
      </div>
    )
  }

  return (
    <div className="store-body">
      <main className="store-main">
        <ProductDetail product={product} onBack={onBack} onAddToCart={onAddToCart} />
      </main>
    </div>
  )
}
