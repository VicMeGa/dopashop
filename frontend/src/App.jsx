import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { fetchProducts } from './api/products.api'
import { fetchCategories } from './api/categories.api'
import useDebounce from './hooks/useDebounce'
import useAuth from './hooks/useAuth'
import AppRoutes from './router/routes'
import Cart from './components/Cart'
import './App.css'

function App() {
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useAuth()
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState(null)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [cart, setCart] = useState([])
  const [cartOpen, setCartOpen] = useState(false)
  const [order, setOrder] = useState(null)

  const debouncedSearch = useDebounce(search, 300)

  useEffect(() => {
    fetchCategories().then(setCategories).catch(console.error)
  }, [])

  useEffect(() => {
    setLoading(true)
    fetchProducts({ title: debouncedSearch || undefined, categoryId: categoryId || undefined })
      .then(setProducts)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [debouncedSearch, categoryId])

  function handleSelect(product) {
    setSelectedProduct(product)
    navigate(`/product/${product.id}`)
  }

  function handleBack() {
    setSelectedProduct(null)
    navigate('/')
  }

  function handleGoToCheckout() {
    setCartOpen(false)
    navigate('/checkout')
  }

  function handleBackToCart() {
    navigate('/')
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
    navigate(`/order-confirmed/${newOrder.orderId}`)
  }

  function handleBackToCatalog() {
    setSearch('')
    setCategoryId(null)
    setOrder(null)
    setSelectedProduct(null)
    navigate('/')
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
        <h1 onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>dopaShop</h1>
        <input
          type="text"
          placeholder="Buscar productos..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <div className="header-auth">
          {isAuthenticated ? (
            <>
              <span className="header-user">{user.fullName}</span>
              <button className="header-link" onClick={() => { logout(); navigate('/') }}>Cerrar sesión</button>
            </>
          ) : (
            <>
              <Link to="/login" className="header-link">Iniciar sesión</Link>
              <Link to="/register" className="header-link">Registrarse</Link>
            </>
          )}
        </div>
        <button className="cart-icon-btn" onClick={() => setCartOpen(true)}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6" />
          </svg>
          {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
        </button>
      </header>

      <AppRoutes
        products={products}
        loading={loading}
        categories={categories}
        categoryId={categoryId}
        selectedProduct={selectedProduct}
        order={order}
        cart={cart}
        onCategoryChange={setCategoryId}
        onSelect={handleSelect}
        onBack={handleBack}
        onAddToCart={addToCart}
        onBackToCart={handleBackToCart}
        onConfirm={handleConfirm}
        onBackToCatalog={handleBackToCatalog}
      />

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
