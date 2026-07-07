import { useState, useEffect } from 'react'
import { fetchProducts, fetchCategories } from './api'
import ProductCard from './components/ProductCard'
import ProductDetail from './components/ProductDetail'
import CategorySidebar from './components/CategorySidebar'
import Cart from './components/Cart'
import Checkout from './components/Checkout'
import ConfirmedOrder from './components/ConfirmedOrder'
import './App.css'

function App() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState(null)
  const [view, setView] = useState('list')
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [cart, setCart] = useState([])
  const [cartOpen, setCartOpen] = useState(false)
  const [order, setOrder] = useState(null)

  useEffect(() => {
    fetchCategories().then(setCategories).catch(console.error)
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      fetchProducts({ title: search || undefined, categoryId: categoryId || undefined })
        .then(setProducts)
        .catch(console.error)
        .finally(() => setLoading(false))
    }, 300)
    return () => clearTimeout(timer)
  }, [search, categoryId])

  function handleSelect(product) {
    setSelectedProduct(product)
    setView('detail')
  }

  function handleBack() {
    setView('list')
    setSelectedProduct(null)
  }

  function handleGoToCheckout() {
    setCartOpen(false)
    setView('checkout')
  }
 
  function handleBackToCart() {
    setView('list')
    setCartOpen(true)
  }

  function handleConfirm() {
    if (cart.length === 0) return
    const newOrder = {
      orderId: `ORD-${Date.now()}-${Math.random().toString(36).slice(2, 6).toUpperCase()}`,
      items: cart.map(item => ({
        product: { ...item.product },
        quantity: item.quantity,
      })),
      total: cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0),
      status: 'COMPLETED',
      createdAt: new Date().toISOString(),
    }
    setOrder(newOrder)
    setCart([])
    setView('confirmed')
  }

  function handleBackToCatalog() {
    setView('list')
    setSearch('')
    setCategoryId(null)
    setOrder(null)
  }

  function addToCart(product) {
    setCart(prev => {
      const exists = prev.find(item => item.product.id === product.id)
      if (exists) {
        return prev.map(item =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        )
      }
      return [...prev, { product, quantity: 1 }]
    })
  }

  function increaseQty(id) {
    setCart(prev => prev.map(item =>
      item.product.id === id
        ? { ...item, quantity: item.quantity + 1 }
        : item
    ))
  }

  function decreaseQty(id) {
    setCart(prev => {
      const item = prev.find(i => i.product.id === id)
      if (item?.quantity <= 1) return prev.filter(i => i.product.id !== id)
      return prev.map(i =>
        i.product.id === id
          ? { ...i, quantity: i.quantity - 1 }
          : i
      )
    })
  }

  function removeFromCart(id) {
    setCart(prev => prev.filter(i => i.product.id !== id))
  }

  function clearCart() {
    setCart([])
  }

  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <div className="store">
      <header className="store-header">
        <h1>dopaShop</h1>
        <input
          type="text"
          placeholder="Buscar productos..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button className="cart-icon-btn" onClick={() => setCartOpen(true)}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6" />
          </svg>
          {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
        </button>
      </header>

      <div className="store-body">
        <CategorySidebar
          categories={categories}
          selected={categoryId}
          onSelect={setCategoryId}
        />

        <main className="store-main">
          {view === 'confirmed' && order ? (
            <ConfirmedOrder order={order} onBackToCatalog={handleBackToCatalog} />
          ) : view === 'checkout' ? (
            <Checkout cart={cart} onBackToCart={handleBackToCart} onConfirm={handleConfirm} />
          ) : view === 'detail' && selectedProduct ? (
            <ProductDetail product={selectedProduct} onBack={handleBack} onAddToCart={addToCart} />
          ) : loading ? (
            <p className="store-loading">Cargando productos...</p>
          ) : (
            <div className="product-grid">
              {products.map((p) => (
                <ProductCard key={p.id} product={p} onSelect={handleSelect} onAddToCart={addToCart} />
              ))}
            </div>
          )}
        </main>
      </div>

      {cartOpen && (
        <Cart
          cart={cart}
          onIncreaseQty={increaseQty}
          onDecreaseQty={decreaseQty}
          onRemove={removeFromCart}
          onClear={clearCart}
          onClose={() => setCartOpen(false)}
          onCheckout={handleGoToCheckout}
        />
      )}
    </div>
  )
}

export default App
