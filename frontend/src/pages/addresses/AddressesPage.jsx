import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import * as addressesApi from '../../api/addresses.api'
import AddressForm from './components/AddressForm'

export default function AddressesPage() {
  const [addresses, setAddresses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)

  async function load() {
    try {
      setError('')
      const data = await addressesApi.getAddresses()
      setAddresses(data)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  async function handleCreate(data) {
    await addressesApi.createAddress(data)
    setShowForm(false)
    load()
  }

  async function handleUpdate(id, data) {
    await addressesApi.updateAddress(id, data)
    setEditing(null)
    load()
  }

  async function handleDelete(id) {
    setDeleting(id)
    try {
      await addressesApi.deleteAddress(id)
      load()
    } catch (e) {
      setError(e.message)
    } finally {
      setDeleting(null)
    }
  }

  if (loading) return <div className="store-loading">Cargando direcciones...</div>

  return (
    <div className="store-body">
      <main className="store-main">
        <div className="page-header">
          <h2>Mis direcciones</h2>
          <button className="btn-primary" onClick={() => { setEditing(null); setShowForm(!showForm) }}>
            {showForm ? 'Cancelar' : 'Agregar dirección'}
          </button>
        </div>

        {error && <p className="auth-error">{error}</p>}

        {showForm && (
          <AddressForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        )}

        {editing && (
          <AddressForm
            initialValues={editing}
            onSubmit={(data) => handleUpdate(editing.id, data)}
            onCancel={() => setEditing(null)}
          />
        )}

        <div className="addresses-list">
          {addresses.length === 0 && !showForm && (
            <p className="empty-state">No tenés direcciones guardadas.</p>
          )}
          {addresses.map(addr => (
            <div key={addr.id} className="address-card">
              <div className="address-card-body">
                <p className="address-card-street">{addr.street}</p>
                <p className="address-card-detail">{addr.city}, {addr.state} {addr.zipCode}</p>
                <p className="address-card-detail">{addr.country}</p>
                {addr.isDefault && <span className="badge">Predeterminada</span>}
              </div>
              <div className="address-card-actions">
                <button className="btn-secondary" onClick={() => { setShowForm(false); setEditing(addr) }}>Editar</button>
                <button className="btn-danger" onClick={() => handleDelete(addr.id)} disabled={deleting === addr.id}>
                  {deleting === addr.id ? '...' : 'Eliminar'}
                </button>
              </div>
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
