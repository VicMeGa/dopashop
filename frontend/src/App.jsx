import { useState, useEffect } from 'react'
import { fetchProducts, fetchCategories } from './api'
import ProductCard from './components/ProductCard'
import ProductDetail from './components/ProductDetail'
import CategorySidebar from './components/CategorySidebar'
import './App.css'

function App() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState(null)
  const [view, setView] = useState('list')
  const [selectedProduct, setSelectedProduct] = useState(null)

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
      </header>

      <div className="store-body">
        <CategorySidebar
          categories={categories}
          selected={categoryId}
          onSelect={setCategoryId}
        />

        <main className="store-main">
          {view === 'detail' && selectedProduct ? (
            <ProductDetail product={selectedProduct} onBack={handleBack} />
          ) : loading ? (
            <p className="store-loading">Cargando productos...</p>
          ) : (
            <div className="product-grid">
              {products.map((p) => (
                <ProductCard key={p.id} product={p} onSelect={handleSelect} />
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  )
}

export default App
