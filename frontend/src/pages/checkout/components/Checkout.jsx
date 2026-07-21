import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import * as addressesApi from '../../../api/addresses.api'
import * as pmApi from '../../../api/paymentMethods.api'
import * as ordersApi from '../../../api/orders.api'

export default function Checkout({ cart, onBackToCart, clearCart }) {
  const navigate = useNavigate()
  const [addresses, setAddresses] = useState([])
  const [paymentMethods, setPaymentMethods] = useState([])
  const [selectedAddressId, setSelectedAddressId] = useState(null)
  const [selectedPmId, setSelectedPmId] = useState(null)
  const [loadingData, setLoadingData] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [pendingOrderId, setPendingOrderId] = useState(null)
  const [paymentRejected, setPaymentRejected] = useState(false)

  useEffect(() => {
    async function load() {
      try {
        const [addrs, pms] = await Promise.all([
          addressesApi.getAddresses(),
          pmApi.getPaymentMethods(),
        ])
        setAddresses(addrs)
        setPaymentMethods(pms)
        const defaultAddr = addrs.find(a => a.isDefault)
        if (defaultAddr) setSelectedAddressId(defaultAddr.id)
        else if (addrs.length > 0) setSelectedAddressId(addrs[0].id)
        if (pms.length > 0) setSelectedPmId(pms[0].id)
      } catch (e) {
        setError(e.message)
      } finally {
        setLoadingData(false)
      }
    }
    load()
  }, [])

  const itemsCount = cart.reduce((sum, item) => sum + item.quantity, 0)
  const total = cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0)

  if (cart.length === 0) {
    return (
      <div className="checkout-empty">
        <p>Tu carrito está vacío</p>
        <button className="checkout-back-btn" onClick={onBackToCart}>Volver a la tienda</button>
      </div>
    )
  }

  if (loadingData) {
    return <div className="store-loading">Cargando datos de envío y pago...</div>
  }

  if (addresses.length === 0) {
    return (
      <div className="checkout-empty">
        <p>Necesitás una dirección de envío para continuar.</p>
        <Link to="/addresses" className="checkout-confirm-btn" style={{ textDecoration: 'none', display: 'inline-block', marginTop: 16 }}>
          Agregar dirección
        </Link>
      </div>
    )
  }

  if (paymentMethods.length === 0) {
    return (
      <div className="checkout-empty">
        <p>Necesitás un método de pago para continuar.</p>
        <Link to="/payment-methods" className="checkout-confirm-btn" style={{ textDecoration: 'none', display: 'inline-block', marginTop: 16 }}>
          Agregar método de pago
        </Link>
      </div>
    )
  }

  async function handleConfirm() {
    setError('')
    setSubmitting(true)
    setPaymentRejected(false)
    try {
      const items = cart.map(item => ({
        productId: item.product.id,
        quantity: item.quantity,
      }))
      const order = await ordersApi.createOrder({
        addressId: selectedAddressId,
        paymentMethodId: selectedPmId,
        items,
      })
      setPendingOrderId(order.id)
      const payment = await ordersApi.attemptPayment(order.id)
      if (payment.status === 'APPROVED') {
        clearCart()
        navigate(`/order-confirmed/${order.id}`)
      } else {
        setPaymentRejected(true)
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRetryPayment() {
    if (!pendingOrderId) return
    setError('')
    setSubmitting(true)
    try {
      const payment = await ordersApi.attemptPayment(pendingOrderId)
      if (payment.status === 'APPROVED') {
        clearCart()
        navigate(`/order-confirmed/${pendingOrderId}`)
      } else {
        setPaymentRejected(true)
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="checkout">
      <h2 className="checkout-title">Checkout</h2>

      {error && <p className="auth-error">{error}</p>}

      <div className="checkout-section">
        <h3>Dirección de envío</h3>
        <div className="checkout-options">
          {addresses.map(addr => (
            <label key={addr.id} className={`checkout-option ${selectedAddressId === addr.id ? 'selected' : ''}`}>
              <input type="radio" name="address" checked={selectedAddressId === addr.id} onChange={() => setSelectedAddressId(addr.id)} />
              <div>
                <p className="checkout-option-label">{addr.street}</p>
                <p className="checkout-option-detail">{addr.city}, {addr.state} {addr.zipCode}</p>
                {addr.isDefault && <span className="badge">Predeterminada</span>}
              </div>
            </label>
          ))}
        </div>
        <Link to="/addresses" className="header-link">Gestionar direcciones</Link>
      </div>

      <div className="checkout-section">
        <h3>Método de pago</h3>
        <div className="checkout-options">
          {paymentMethods.map(pm => (
            <label key={pm.id} className={`checkout-option ${selectedPmId === pm.id ? 'selected' : ''}`}>
              <input type="radio" name="paymentMethod" checked={selectedPmId === pm.id} onChange={() => setSelectedPmId(pm.id)} />
              <span className="payment-card-brand">{pm.cardBrand}</span>
              <span className="payment-card-last4">•••• {pm.cardLastFour}</span>
              <span className="payment-card-alias">{pm.alias}</span>
            </label>
          ))}
        </div>
        <Link to="/payment-methods" className="header-link">Gestionar métodos de pago</Link>
      </div>

      <div className="checkout-section">
        <h3>Resumen</h3>
        <div className="checkout-items">
          {cart.map(item => (
            <div key={item.product.id} className="checkout-item">
              <img className="checkout-item-img" src={item.product.images?.[0] || ''} alt={item.product.title} />
              <div className="checkout-item-info">
                <p className="checkout-item-title">{item.product.title}</p>
                <p className="checkout-item-unit">${item.product.price} × {item.quantity}</p>
              </div>
              <p className="checkout-item-subtotal">${(item.product.price * item.quantity).toFixed(2)}</p>
            </div>
          ))}
        </div>
        <div className="checkout-summary">
          <div className="checkout-summary-row">
            <span>Artículos</span>
            <span>{itemsCount}</span>
          </div>
          <div className="checkout-summary-row total">
            <span>Total</span>
            <span>${total.toFixed(2)}</span>
          </div>
        </div>
      </div>

      {paymentRejected && (
        <div className="payment-rejected">
          <p>Pago rechazado. Podés reintentar o modificar el carrito.</p>
        </div>
      )}

      <div className="checkout-actions">
        <button className="checkout-back-btn" onClick={onBackToCart} disabled={submitting}>
          {paymentRejected ? 'Modificar carrito' : 'Regresar al carrito'}
        </button>
        {paymentRejected ? (
          <button className="checkout-confirm-btn" onClick={handleRetryPayment} disabled={submitting}>
            {submitting ? 'Procesando...' : 'Reintentar pago'}
          </button>
        ) : (
          <button className="checkout-confirm-btn" onClick={handleConfirm} disabled={submitting}>
            {submitting ? 'Procesando...' : 'Confirmar compra'}
          </button>
        )}
      </div>
    </div>
  )
}
