import Checkout from './components/Checkout'

export default function CheckoutPage({ cart, onBackToCart, clearCart }) {
  return (
    <div className="store-body">
      <main className="store-main">
        <Checkout cart={cart} onBackToCart={onBackToCart} clearCart={clearCart} />
      </main>
    </div>
  )
}
