import { useParams } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { getOrderById } from '../../api/orders.api'
import ConfirmedOrder from './components/ConfirmedOrder'

export default function OrderConfirmedPage({ onBackToCatalog }) {
  const { orderId } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      try {
        const data = await getOrderById(orderId)
        setOrder(data)
      } catch (e) {
        setError(e.message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [orderId])

  if (loading) return <div className="store-loading">Cargando orden...</div>
  if (error) return <div className="store-loading" style={{ color: '#e74c3c' }}>{error}</div>
  if (!order) return null

  return (
    <div className="store-body">
      <main className="store-main">
        <ConfirmedOrder order={order} onBackToCatalog={onBackToCatalog} />
      </main>
    </div>
  )
}
