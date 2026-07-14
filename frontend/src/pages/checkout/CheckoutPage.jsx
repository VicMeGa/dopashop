import Checkout from './components/Checkout'

export default function CheckoutPage({ cart, onBackToCart, onConfirm }) {
  return (
    <div className="store-body">
      <main className="store-main">
        <Checkout cart={cart} onBackToCart={onBackToCart} onConfirm={onConfirm} />
      </main>
    </div>
  )
}
