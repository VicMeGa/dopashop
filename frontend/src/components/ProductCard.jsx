export default function ProductCard({ product, onSelect }) {
  const img = product.images?.[0] || ''
  return (
    <article className="product-card" onClick={() => onSelect(product)}>
      <div className="product-card-img">
        <img src={img} alt={product.title} loading="lazy" />
      </div>
      <div className="product-card-body">
        <span className="product-card-category">{product.category?.name}</span>
        <h3 className="product-card-title">{product.title}</h3>
        <p className="product-card-price">${product.price}</p>
      </div>
    </article>
  )
}
