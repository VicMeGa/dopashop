import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getMyOrders } from '../../api/orders.api'
import OrderStatusBadge from '../../components/OrderStatusBadge'

function formatDate(iso) {
  const date = new Date(iso)
  const day = date.getDate()
  const month = date.toLocaleString('es-ES', { month: 'long' })
  const year = date.getFullYear()
  return `${day} de ${month}, ${year}`
}

function itemsLabel(count) {
  return count === 1 ? '1 producto' : `${count} productos`
}

export default function OrdersHistoryPage() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    getMyOrders()
      .then(data => { if (active) setOrders(data) })
      .catch(e => { if (active) setError(e.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  if (loading) return <div className="store-loading">Cargando órdenes...</div>

  return (
    <div className="store-body">
      <main className="store-main">
        <div className="page-header">
          <h2>Historial de órdenes</h2>
        </div>

        {error && <p className="auth-error">{error}</p>}

        {orders.length === 0 && !error && (
          <div className="empty-state orders-empty">
            <p>Aún no has realizado ninguna compra</p>
            <Link to="/" className="btn-primary orders-empty-link">Ir al catálogo</Link>
          </div>
        )}

        <div className="orders-history">
          {orders.map(order => (
            <article key={order.id} className="order-card" onClick={() => navigate(`/orders/${order.id}`)}>
              <div className="order-card-head">
                <span className="order-card-number">Orden #{order.id}</span>
                <span className="order-card-date">{formatDate(order.createdAt)}</span>
              </div>
              <div className="order-card-body">
                <span className="order-card-total">${order.total.toFixed(2)}</span>
                <div className="order-card-badges">
                  <OrderStatusBadge status={order.status} type="order" />
                  {order.shipment && (
                    <OrderStatusBadge status={order.shipment.status} type="shipment" />
                  )}
                </div>
              </div>
              <div className="order-card-foot">
                {itemsLabel(order.items.length)}
              </div>
            </article>
          ))}
        </div>
      </main>
    </div>
  )
}
