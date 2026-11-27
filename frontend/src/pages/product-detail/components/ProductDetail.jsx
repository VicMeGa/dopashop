import { useState } from 'react'
import { Link } from 'react-router-dom'
import ReviewList from './ReviewList'
import ReviewForm from './ReviewForm'

export default function ProductDetail({ product, onBack, onAddToCart }) {
  const [reviewsVersion, setReviewsVersion] = useState(0)
  const img = product.images?.[0] || ''
  return (
    <div className="product-detail">
      <button className="back-btn" onClick={onBack}>&larr; Volver</button>
      <div className="product-detail-content">
        <div className="product-detail-img">
          <img src={img} alt={product.title} />
        </div>
        <div className="product-detail-info">
          <span className="product-detail-category">{product.category?.name}</span>
          <h2>{product.title}</h2>
          {product.storeName && (
            <p className="product-detail-store">
              Vendido por: <Link to={`/stores/${product.storeId}`}>{product.storeName}</Link>
            </p>
          )}
          <p className="product-detail-price">${product.price}</p>
          <p className={`product-detail-stock ${product.stock === 0 ? 'out-of-stock' : product.stock < 5 ? 'low-stock' : ''}`}>
            {product.stock === 0 ? 'Agotado' : product.stock < 5 ? `¡Últimas ${product.stock} unidades!` : `Stock disponible: ${product.stock}`}
          </p>
          <p className="product-detail-desc">{product.description}</p>
          <button 
            className="add-to-cart-btn detail" 
            onClick={() => onAddToCart(product)}
            disabled={product.stock === 0}
          >
            {product.stock === 0 ? 'Sin stock' : 'Agregar al carrito'}
          </button>
        </div>
      </div>

      <ReviewList productId={product.id} refreshKey={reviewsVersion} />
      <ReviewForm productId={product.id} onSuccess={() => setReviewsVersion(v => v + 1)} />
    </div>
  )
}
