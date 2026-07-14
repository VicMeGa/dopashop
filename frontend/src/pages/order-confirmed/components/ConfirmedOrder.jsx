export default function ConfirmedOrder({ order, onBackToCatalog }) {
  const date = new Date(order.createdAt)

  return (
    <div className="confirmed-order">
      <div className="confirmed-header">
        <svg className="confirmed-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M22 11.08V12a10 10 0 11-5.93-9.14" />
          <path d="M22 4L12 14.01l-3-3" />
        </svg>
        <h2>¡Compra realizada!</h2>
      </div>

      <div className="confirmed-meta">
        <div className="confirmed-meta-row">
          <span className="confirmed-meta-label">Nro. de orden</span>
          <span className="confirmed-number">{order.orderId}</span>
        </div>
        <div className="confirmed-meta-row">
          <span className="confirmed-meta-label">Fecha</span>
          <span>{date.toLocaleDateString()} {date.toLocaleTimeString()}</span>
        </div>
        <div className="confirmed-meta-row">
          <span className="confirmed-meta-label">Estado</span>
          <span className="confirmed-status">{order.status}</span>
        </div>
      </div>

      <div className="confirmed-items">
        <h3>Productos</h3>
        {order.items.map(item => (
          <div key={item.product.id} className="confirmed-item">
            <img className="confirmed-item-img" src={item.product.images?.[0] || ''} alt={item.product.title} />
            <div className="confirmed-item-info">
              <p className="confirmed-item-title">{item.product.title}</p>
              <p className="confirmed-item-unit">${item.product.price} × {item.quantity}</p>
            </div>
            <p className="confirmed-item-subtotal">${(item.product.price * item.quantity).toFixed(2)}</p>
          </div>
        ))}
      </div>

      <div className="confirmed-total">
        <span>Total pagado</span>
        <span>${order.total.toFixed(2)}</span>
      </div>

      <button className="confirmed-btn" onClick={onBackToCatalog}>Volver al catálogo</button>
    </div>
  )
}
