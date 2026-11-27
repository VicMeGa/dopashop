import { useState, useEffect } from 'react'
import { useNavigate, Link, useSearchParams } from 'react-router-dom'
import { fetchProducts } from './api/products.api'
import { fetchCategories } from './api/categories.api'
import useDebounce from './hooks/useDebounce'
import useAuth from './hooks/useAuth'
import useCart from './hooks/useCart'
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
  const [cartOpen, setCartOpen] = useState(false)
  const {
    items: cart,
    itemCount: cartCount,
    add: addToCart,
    increase: increaseQty,
    decrease: decreaseQty,
    remove: removeFromCart,
    clear: clearCart,
  } = useCart()

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
    navigate(`/product/${product.id}`)
  }

  function handleBack() {
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
    navigate('/')
  }

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
              <Link to="/orders" className="header-link">Mis órdenes</Link>
              <Link to="/payment-methods" className="header-link">Pagos</Link>
              <Link to="/my-store" className="header-link">Mi tienda</Link>
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
