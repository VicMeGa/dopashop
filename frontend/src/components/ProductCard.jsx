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
        <p className="product-card-price">${product.price}</p>
        <button className="add-to-cart-btn" onClick={(e) => { e.stopPropagation(); onAddToCart(product) }}>
          Agregar
        </button>
      </div>
    </article>
  )
}
