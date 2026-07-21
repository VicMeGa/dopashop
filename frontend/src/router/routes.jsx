import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute'
import CatalogPage from '../pages/catalog/CatalogPage'
import ProductDetailPage from '../pages/product-detail/ProductDetailPage'
import CheckoutPage from '../pages/checkout/CheckoutPage'
import OrderConfirmedPage from '../pages/order-confirmed/OrderConfirmedPage'
import AddressesPage from '../pages/addresses/AddressesPage'
import PaymentMethodsPage from '../pages/payment-methods/PaymentMethodsPage'
import LoginPage from '../pages/auth/LoginPage'
import RegisterPage from '../pages/auth/RegisterPage'

export default function AppRoutes({ products, loading, categories, selectedProduct, cart, onSelect, onBack, onAddToCart, onBackToCart, clearCart, onBackToCatalog }) {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <CatalogPage
            products={products}
            loading={loading}
            categories={categories}
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
          <ProtectedRoute>
            <CheckoutPage cart={cart} onBackToCart={onBackToCart} clearCart={clearCart} />
          </ProtectedRoute>
        }
      />
      <Route
        path="/order-confirmed/:orderId"
        element={
          <ProtectedRoute>
            <OrderConfirmedPage onBackToCatalog={onBackToCatalog} />
          </ProtectedRoute>
        }
      />
      <Route
        path="/addresses"
        element={
          <ProtectedRoute>
            <AddressesPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/payment-methods"
        element={
          <ProtectedRoute>
            <PaymentMethodsPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}
