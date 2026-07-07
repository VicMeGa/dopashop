export default function Cart({ cart, onIncreaseQty, onDecreaseQty, onRemove, onClear, onClose }) {
  const total = cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0)

  return (
    <>
      <div className="cart-overlay" onClick={onClose} />
      <aside className="cart-drawer">
        <div className="cart-header">
          <h2>Carrito</h2>
          <button className="cart-close" onClick={onClose}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>

        {cart.length === 0 ? (
          <div className="cart-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.4">
              <circle cx="9" cy="21" r="1" />
              <circle cx="20" cy="21" r="1" />
              <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6" />
            </svg>
            <p>Tu carrito está vacío</p>
          </div>
        ) : (
          <>
            <div className="cart-items">
              {cart.map(item => (
                <div key={item.product.id} className="cart-item">
                  <img className="cart-item-img" src={item.product.images?.[0] || ''} alt={item.product.title} />
                  <div className="cart-item-info">
                    <p className="cart-item-title">{item.product.title}</p>
                    <p className="cart-item-price">${item.product.price}</p>
                  </div>
                  <div className="cart-item-controls">
                    <button onClick={() => onDecreaseQty(item.product.id)} disabled={item.quantity <= 1}>−</button>
                    <span>{item.quantity}</span>
                    <button onClick={() => onIncreaseQty(item.product.id)}>+</button>
                  </div>
                  <p className="cart-item-subtotal">${(item.product.price * item.quantity).toFixed(2)}</p>
                  <button className="cart-item-remove" onClick={() => onRemove(item.product.id)}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
            <div className="cart-footer">
              <div className="cart-total">
                <span>Total</span>
                <span>${total.toFixed(2)}</span>
              </div>
              <button className="cart-clear" onClick={onClear}>Vaciar carrito</button>
              <button className="cart-continue" onClick={onClose}>Seguir comprando</button>
            </div>
          </>
        )}
      </aside>
    </>
  )
}
