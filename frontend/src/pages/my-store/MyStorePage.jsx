import { useCallback, useEffect, useState } from 'react'
import {
  getMyStore,
  createStore,
  getMyProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  reactivateProduct,
} from '../../api/myStore.api'
import ProductForm from './components/ProductForm'

function formatPrice(price) {
  return `$${Number(price).toFixed(2)}`
}

export default function MyStorePage() {
  const [store, setStore] = useState(undefined)
  const [storeError, setStoreError] = useState('')
  const [storeForm, setStoreForm] = useState({ name: '', description: '' })
  const [creatingStore, setCreatingStore] = useState(false)

  const [products, setProducts] = useState([])
  const [productsLoading, setProductsLoading] = useState(false)
  const [listError, setListError] = useState('')

  const [showCreateForm, setShowCreateForm] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)

  useEffect(() => {
    let cancelled = false
    getMyStore()
      .then(data => {
        if (!cancelled) setStore(data)
      })
      .catch(err => {
        if (cancelled) return
        setStore(null)
        setStoreError(err.message || 'Error al cargar tu tienda')
      })
    return () => {
      cancelled = true
    }
  }, [])

  const loadProducts = useCallback(async () => {
    setProductsLoading(true)
    setListError('')
    try {
      setProducts(await getMyProducts())
    } catch (err) {
      setListError(err.message || 'Error al cargar tus productos')
    } finally {
      setProductsLoading(false)
    }
  }, [])

  useEffect(() => {
    if (store) loadProducts()
  }, [store, loadProducts])

  function handleStoreFieldChange(e) {
    const { name, value } = e.target
    setStoreForm(prev => ({ ...prev, [name]: value }))
  }

  async function handleCreateStore(e) {
    e.preventDefault()
    setStoreError('')
    if (!storeForm.name.trim()) {
      setStoreError('El nombre de la tienda es obligatorio')
      return
    }
    setCreatingStore(true)
    try {
      const created = await createStore({
        name: storeForm.name.trim(),
        description: storeForm.description.trim(),
      })
      setStore(created)
    } catch (err) {
      setStoreError(err.message || 'Error al crear la tienda')
    } finally {
      setCreatingStore(false)
    }
  }

  async function handleCreateSubmit(payload) {
    await createProduct(payload)
    setShowCreateForm(false)
    await loadProducts()
  }

  async function handleUpdateSubmit(payload) {
    await updateProduct(editingProduct.id, payload)
    setEditingProduct(null)
    await loadProducts()
  }

  async function handleDeactivate(product) {
    if (!window.confirm(`¿Desactivar el producto "${product.title}"? Dejará de verse en el catálogo.`)) return
    setListError('')
    try {
      await deleteProduct(product.id)
      await loadProducts()
    } catch (err) {
      setListError(err.message || 'Error al desactivar el producto')
    }
  }

  async function handleReactivate(product) {
    setListError('')
    try {
      await reactivateProduct(product.id)
      await loadProducts()
    } catch (err) {
      setListError(err.message || 'Error al reactivar el producto')
    }
  }

  function closeForms() {
    setShowCreateForm(false)
    setEditingProduct(null)
  }

  if (store === undefined) {
    return (
      <div className="store-body">
        <main className="store-main">
          <div className="store-loading">Cargando tu tienda...</div>
        </main>
      </div>
    )
  }

  if (store === null) {
    return (
      <div className="store-body">
        <main className="store-main">
          <div className="page-header">
            <h2>Crea tu tienda</h2>
          </div>
          {storeError && <p className="auth-error">{storeError}</p>}
          <form className="my-store-create-form" onSubmit={handleCreateStore}>
            <div className="auth-field">
              <label htmlFor="store-name">Nombre de la tienda</label>
              <input
                id="store-name"
                name="name"
                value={storeForm.name}
                onChange={handleStoreFieldChange}
                placeholder="Ej: Componentes Víctor"
              />
            </div>
            <div className="auth-field">
              <label htmlFor="store-description">Descripción</label>
              <textarea
                id="store-description"
                name="description"
                rows={3}
                value={storeForm.description}
                onChange={handleStoreFieldChange}
                placeholder="¿Qué vendes en tu tienda?"
              />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={creatingStore}>
                {creatingStore ? 'Creando...' : 'Crear tienda'}
              </button>
            </div>
          </form>
        </main>
      </div>
    )
  }

  const initialValues = editingProduct
    ? {
        title: editingProduct.title,
        description: editingProduct.description,
        price: editingProduct.price,
        stock: editingProduct.stock,
        categoryId: editingProduct.category?.id,
        imageUrl: editingProduct.images?.[0],
      }
    : undefined

  return (
    <div className="store-body">
      <main className="store-main">
        <div className="page-header">
          <h2>{store.name}</h2>
          {!showCreateForm && !editingProduct && (
            <button className="btn-primary" onClick={() => setShowCreateForm(true)}>
              Agregar producto
            </button>
          )}
        </div>

        {store.description && <p className="my-store-description">{store.description}</p>}

        {(showCreateForm || editingProduct) && (
          <section className="my-store-form-panel">
            <h3>{editingProduct ? `Editar producto` : 'Nuevo producto'}</h3>
            <ProductForm
              key={editingProduct ? editingProduct.id : 'new'}
              initialValues={initialValues}
              onSubmit={editingProduct ? handleUpdateSubmit : handleCreateSubmit}
              onCancel={closeForms}
            />
          </section>
        )}

        <section>
          <h3>Productos ({products.length})</h3>
          {productsLoading && <p>Cargando productos...</p>}
          {listError && <p className="auth-error">{listError}</p>}
          {!productsLoading && !listError && !showCreateForm && products.length === 0 && (
            <p className="empty-state">Aún no tienes productos publicados.</p>
          )}
          <ul className="my-store-products">
            {products.map(product => (
              <li key={product.id} className="my-store-product">
                {product.images?.[0] ? (
                  <img src={product.images[0]} alt={product.title} />
                ) : (
                  <div className="my-store-product-placeholder">Sin imagen</div>
                )}
                <div className="my-store-product-info">
                  <strong>{product.title}</strong>
                  <span>{formatPrice(product.price)}</span>
                  <span>Stock: {product.stock}</span>
                </div>
                <span
                  className={`order-status-badge ${
                    product.active ? 'order-status-badge--green' : 'order-status-badge--gray'
                  }`}
                >
                  {product.active ? 'Activo' : 'Desactivado'}
                </span>
                <div className="my-store-product-actions">
                  <button className="btn-secondary" onClick={() => { setEditingProduct(product); setShowCreateForm(false) }}>
                    Editar
                  </button>
                  {product.active ? (
                    <button className="btn-danger" onClick={() => handleDeactivate(product)}>
                      Desactivar
                    </button>
                  ) : (
                    <button className="btn-primary" onClick={() => handleReactivate(product)}>
                      Reactivar
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </section>
      </main>
    </div>
  )
}
