import OrderStatusBadge from '../../../components/OrderStatusBadge'

function formatDate(iso) {
  const date = new Date(iso)
  const day = date.getDate()
  const month = date.toLocaleString('es-ES', { month: 'long' })
  const year = date.getFullYear()
  return `${day} de ${month}, ${year}`
}

export default function OrderDetailView({ order, onBack }) {
  return (
    <div className="order-detail">
      <button className="back-btn" onClick={onBack}>← Volver al historial</button>

      <div className="page-header">
        <h2>Orden #{order.id}</h2>
        <OrderStatusBadge status={order.status} type="order" />
      </div>

      <div className="confirmed-meta">
        <div className="confirmed-meta-row">
          <span className="confirmed-meta-label">Fecha</span>
          <span>{formatDate(order.createdAt)}</span>
        </div>
      </div>

      <div className="confirmed-items">
        <h3>Productos</h3>
        {order.items.map((item, i) => (
          <div key={i} className="confirmed-item">
            {item.imageUrl ? (
              <img className="confirmed-item-img" src={item.imageUrl} alt={item.productTitle} />
            ) : (
              <div className="confirmed-item-img order-item-placeholder">Sin imagen</div>
            )}
            <div className="confirmed-item-info">
              <p className="confirmed-item-title">{item.productTitle}</p>
              <p className="confirmed-item-unit">${item.unitPrice.toFixed(2)} × {item.quantity}</p>
            </div>
            <p className="confirmed-item-subtotal">${item.subtotal.toFixed(2)}</p>
          </div>
        ))}
      </div>

      <div className="order-detail-grid">
        <div className="confirmed-meta">
          <h3 className="order-detail-section-title">Envío a</h3>
          <p className="order-detail-text">{order.address.street}</p>
          <p className="order-detail-text">{order.address.city}, {order.address.state} {order.address.zipCode}</p>
          <p className="order-detail-text">{order.address.country}</p>
        </div>

        <div className="confirmed-meta">
          <h3 className="order-detail-section-title">Pago</h3>
          <p className="order-detail-text">{order.paymentMethod.alias}</p>
          <p className="order-detail-text">{order.paymentMethod.cardBrand} •••• {order.paymentMethod.cardLastFour}</p>
        </div>
      </div>

      {order.shipment && (
        <div className="confirmed-meta order-detail-shipment">
          <h3 className="order-detail-section-title">Envío</h3>
          <div className="confirmed-meta-row">
            <span className="confirmed-meta-label">Estado</span>
            <OrderStatusBadge status={order.shipment.status} type="shipment" />
          </div>
          {order.shipment.trackingCode && (
            <div className="confirmed-meta-row">
              <span className="confirmed-meta-label">Seguimiento</span>
              <span className="confirmed-number">{order.shipment.trackingCode}</span>
            </div>
          )}
        </div>
      )}

      <div className="confirmed-total">
        <span>Total</span>
        <span>${order.total.toFixed(2)}</span>
      </div>

      <button className="confirmed-btn" onClick={onBack}>Volver al historial</button>
    </div>
  )
}
