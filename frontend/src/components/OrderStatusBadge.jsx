const ORDER_STATUS = {
  PENDING: { label: 'Pendiente', tone: 'gray' },
  CONFIRMED: { label: 'Confirmada', tone: 'green' },
  CANCELLED: { label: 'Cancelada', tone: 'red' },
}

const SHIPMENT_STATUS = {
  PENDING: { label: 'Pendiente', tone: 'gray' },
  PREPARING: { label: 'Preparando', tone: 'blue' },
  SHIPPED: { label: 'Enviado', tone: 'purple' },
  DELIVERED: { label: 'Entregado', tone: 'green' },
}

export default function OrderStatusBadge({ status, type = 'order' }) {
  const statuses = type === 'shipment' ? SHIPMENT_STATUS : ORDER_STATUS
  const config = statuses[status] || { label: status, tone: 'gray' }
  return <span className={`order-status-badge order-status-badge--${config.tone}`}>{config.label}</span>
}
