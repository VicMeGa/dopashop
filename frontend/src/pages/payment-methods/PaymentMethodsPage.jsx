import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import * as pmApi from '../../api/paymentMethods.api'
import CardForm from './components/CardForm'

export default function PaymentMethodsPage() {
  const [methods, setMethods] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [deleting, setDeleting] = useState(null)

  async function load() {
    try {
      const data = await pmApi.getPaymentMethods()
      setMethods(data)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  async function handleCreate(data) {
    await pmApi.createPaymentMethod(data)
    setShowForm(false)
    load()
  }

  async function handleDelete(id) {
    setDeleting(id)
    try {
      await pmApi.deletePaymentMethod(id)
      load()
    } catch (e) {
      setError(e.message)
    } finally {
      setDeleting(null)
    }
  }

  if (loading) return <div className="store-loading">Cargando métodos de pago...</div>

  return (
    <div className="store-body">
      <main className="store-main">
        <div className="page-header">
          <h2>Mis métodos de pago</h2>
          <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancelar' : 'Agregar tarjeta'}
          </button>
        </div>

        {error && <p className="auth-error">{error}</p>}

        {showForm && (
          <CardForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        )}

        <div className="payment-methods-list">
          {methods.length === 0 && !showForm && (
            <p className="empty-state">No tenés métodos de pago guardados.</p>
          )}
          {methods.map(pm => (
            <div key={pm.id} className="payment-card">
              <div className="payment-card-info">
                <span className="payment-card-brand">{pm.cardBrand}</span>
                <span className="payment-card-alias">{pm.alias}</span>
                <span className="payment-card-last4">•••• {pm.cardLastFour}</span>
              </div>
              <button className="btn-danger" onClick={() => handleDelete(pm.id)} disabled={deleting === pm.id}>
                {deleting === pm.id ? '...' : 'Eliminar'}
              </button>
            </div>
          ))}
        </div>

        <Link to="/checkout" className="checkout-back-btn" style={{ display: 'inline-block', marginTop: 20, textAlign: 'center', textDecoration: 'none' }}>
          Volver al checkout
        </Link>
      </main>
    </div>
  )
}
