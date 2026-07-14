import ProductDetail from './components/ProductDetail'

export default function ProductDetailPage({ product, onBack, onAddToCart }) {
  return (
    <div className="store-body">
      <main className="store-main">
        <ProductDetail product={product} onBack={onBack} onAddToCart={onAddToCart} />
      </main>
    </div>
  )
}
