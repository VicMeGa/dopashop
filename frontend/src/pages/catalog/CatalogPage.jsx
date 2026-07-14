import ProductCard from '../../components/ProductCard'
import CategorySidebar from './components/CategorySidebar'

export default function CatalogPage({ products, loading, categories, categoryId, onCategoryChange, onSelect, onAddToCart }) {
  return (
    <div className="store-body">
      <CategorySidebar categories={categories} selected={categoryId} onSelect={onCategoryChange} />
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
