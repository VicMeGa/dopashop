import { useParams, useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { getOrderById } from '../../api/orders.api'
import OrderDetailView from './components/OrderDetailView'

export default function OrderDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      try {
        const data = await getOrderById(id)
        setOrder(data)
      } catch (e) {
        setError(e.message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  if (loading) return <div className="store-loading">Cargando orden...</div>
  if (error) return <div className="store-loading" style={{ color: '#e74c3c' }}>{error}</div>
  if (!order) return null

  return (
    <div className="store-body">
      <main className="store-main">
        <OrderDetailView order={order} onBack={() => navigate('/orders')} />
      </main>
    </div>
  )
}
