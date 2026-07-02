export default function ProductDetail({ product, onBack }) {
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
          <p className="product-detail-price">${product.price}</p>
          <p className="product-detail-desc">{product.description}</p>
        </div>
      </div>
    </div>
  )
}
