import { Link } from 'react-router-dom'

export default function ProductCard({ product, onSelect, onAddToCart }) {
  const img = product.images?.[0] || ''
  return (
    <article className="product-card">
      <div className="product-card-img" onClick={() => onSelect(product)}>
        <img src={img} alt={product.title} loading="lazy" />
      </div>
      <div className="product-card-body">
        <span className="product-card-category">{product.category?.name}</span>
        <h3 className="product-card-title" onClick={() => onSelect(product)}>{product.title}</h3>
        {product.storeName && (
          <p className="product-card-store">
            Vendido por: <Link to={`/stores/${product.storeId}`} onClick={(e) => e.stopPropagation()}>{product.storeName}</Link>
          </p>
        )}
        <p className="product-card-price">${product.price}</p>
        <p className={`product-card-stock ${product.stock === 0 ? 'out-of-stock' : ''}`}>
          {product.stock === 0 ? 'Agotado' : `Stock: ${product.stock}`}
        </p>
        <button 
          className="add-to-cart-btn" 
          onClick={(e) => { e.stopPropagation(); onAddToCart(product) }}
          disabled={product.stock === 0}
        >
          {product.stock === 0 ? 'Sin stock' : 'Agregar'}
        </button>
      </div>
    </article>
  )
}
