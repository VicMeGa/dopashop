import { Routes, Route } from 'react-router-dom'
import CatalogPage from '../pages/catalog/CatalogPage'
import ProductDetailPage from '../pages/product-detail/ProductDetailPage'
import CheckoutPage from '../pages/checkout/CheckoutPage'
import OrderConfirmedPage from '../pages/order-confirmed/OrderConfirmedPage'

export default function AppRoutes({ products, loading, categories, categoryId, selectedProduct, order, cart, onCategoryChange, onSelect, onBack, onAddToCart, onBackToCart, onConfirm, onBackToCatalog }) {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <CatalogPage
            products={products}
            loading={loading}
            categories={categories}
            categoryId={categoryId}
            onCategoryChange={onCategoryChange}
            onSelect={onSelect}
            onAddToCart={onAddToCart}
          />
        }
      />
      <Route
        path="/product/:id"
        element={
          <ProductDetailPage
            product={selectedProduct}
            onBack={onBack}
            onAddToCart={onAddToCart}
          />
        }
      />
      <Route
        path="/checkout"
        element={
          <CheckoutPage
            cart={cart}
            onBackToCart={onBackToCart}
            onConfirm={onConfirm}
          />
        }
      />
      <Route
        path="/order-confirmed/:orderId"
        element={
          <OrderConfirmedPage
            order={order}
            onBackToCatalog={onBackToCatalog}
          />
        }
      />
    </Routes>
  )
}
