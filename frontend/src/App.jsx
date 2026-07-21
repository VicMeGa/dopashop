import { useState, useEffect } from 'react'
import { useNavigate, Link, useSearchParams } from 'react-router-dom'
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
  const [searchParams, setSearchParams] = useSearchParams()
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [cart, setCart] = useState([])
  const [cartOpen, setCartOpen] = useState(false)

  const search = searchParams.get('search') || ''
  const categoryId = searchParams.get('category') || null
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

  function handleBackToCatalog() {
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
          onChange={(e) => {
            const value = e.target.value
            const next = new URLSearchParams(searchParams)
            if (value) next.set('search', value)
            else next.delete('search')
            setSearchParams(next, { replace: true })
          }}
        />
        <div className="header-auth">
          {isAuthenticated ? (
            <>
              <span className="header-user">{user.fullName}</span>
              <Link to="/addresses" className="header-link">Direcciones</Link>
              <Link to="/payment-methods" className="header-link">Pagos</Link>
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
        selectedProduct={selectedProduct}
        cart={cart}
        onSelect={handleSelect}
        onBack={handleBack}
        onAddToCart={addToCart}
        onBackToCart={handleBackToCart}
        clearCart={clearCart}
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
