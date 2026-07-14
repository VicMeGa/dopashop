export default function Checkout({ cart, onBackToCart, onConfirm }) {
  const itemsCount = cart.reduce((sum, item) => sum + item.quantity, 0)
  const total = cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0)

  if (cart.length === 0) {
    return (
      <div className="checkout-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.4">
          <circle cx="9" cy="21" r="1" />
          <circle cx="20" cy="21" r="1" />
          <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6" />
        </svg>
        <p>Tu carrito está vacío</p>
        <p className="checkout-empty-sub">Agregá productos antes de continuar al checkout.</p>
        <button className="checkout-back-btn" onClick={onBackToCart}>Volver a la tienda</button>
      </div>
    )
  }

  return (
    <div className="checkout">
      <h2 className="checkout-title">Checkout</h2>

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

      <div className="checkout-actions">
        <button className="checkout-back-btn" onClick={onBackToCart}>Regresar al carrito</button>
        <button className="checkout-confirm-btn" onClick={onConfirm}>Confirmar compra</button>
      </div>
    </div>
  )
}
