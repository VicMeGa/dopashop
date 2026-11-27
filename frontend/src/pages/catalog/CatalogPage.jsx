import { useSearchParams } from 'react-router-dom'
import ProductCard from '../../components/ProductCard'
import CategorySidebar from './components/CategorySidebar'

export default function CatalogPage({ products, loading, categories, onSelect, onAddToCart }) {
  const [searchParams, setSearchParams] = useSearchParams()
  const categoryId = searchParams.get('category') || null

  function handleCategoryChange(catId) {
    const next = new URLSearchParams(searchParams)
    if (catId) next.set('category', catId)
    else next.delete('category')
    setSearchParams(next)
  }

  return (
    <div className="store-body">
      <CategorySidebar categories={categories} selected={categoryId} onSelect={handleCategoryChange} />
      <main className="store-main">
        {loading ? (
          <p className="store-loading">Cargando productos...</p>
        ) : (
          <div className="product-grid">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} onSelect={onSelect} onAddToCart={onAddToCart} />
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
