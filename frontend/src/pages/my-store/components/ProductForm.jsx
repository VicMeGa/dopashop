import { useEffect, useState } from 'react'
import { fetchCategories } from '../../../api/categories.api'
import { uploadImage } from '../../../api/myStore.api'

const emptyValues = { title: '', description: '', price: '', stock: '', categoryId: '' }

export default function ProductForm({ initialValues, onSubmit, onCancel }) {
  const isEdit = Boolean(initialValues)
  const initialImageUrl = initialValues?.imageUrl || initialValues?.imageUrls?.[0] || null

  const [values, setValues] = useState(() =>
    initialValues
      ? {
          title: initialValues.title ?? '',
          description: initialValues.description ?? '',
          price: String(initialValues.price ?? ''),
          stock: String(initialValues.stock ?? ''),
          categoryId: initialValues.categoryId != null ? String(initialValues.categoryId) : '',
        }
      : { ...emptyValues }
  )
  const [categories, setCategories] = useState([])
  const [file, setFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState(initialImageUrl)
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    fetchCategories()
      .then(data => {
        if (!cancelled) setCategories(data)
      })
      .catch(() => {
        if (!cancelled) setError('No se pudieron cargar las categorías')
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    return () => {
      if (previewUrl && previewUrl.startsWith('blob:')) URL.revokeObjectURL(previewUrl)
    }
  }, [previewUrl])

  function handleChange(e) {
    const { name, value } = e.target
    setValues(prev => ({ ...prev, [name]: value }))
  }

  function handleFileChange(e) {
    const selected = e.target.files[0]
    if (!selected) return
    setFile(selected)
    setPreviewUrl(URL.createObjectURL(selected))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')

    const price = Number(values.price)
    const stock = Number(values.stock)

    if (!values.title.trim()) {
      setError('El título es obligatorio')
      return
    }
    if (values.title.trim().length > 200) {
      setError('El título no puede superar los 200 caracteres')
      return
    }
    if (values.description && values.description.length > 4000) {
      setError('La descripción no puede superar los 4000 caracteres')
      return
    }
    if (values.price === '' || Number.isNaN(price) || price <= 0) {
      setError('El precio debe ser un número mayor a 0')
      return
    }
    if (!/^\d+(\.\d{1,2})?$/.test(values.price)) {
      setError('El precio no puede tener más de 2 decimales')
      return
    }
    if (values.stock === '' || !Number.isInteger(stock) || stock < 0) {
      setError('El stock debe ser un número entero mayor o igual a 0')
      return
    }
    if (!values.categoryId) {
      setError('Selecciona una categoría')
      return
    }

    setLoading(true)
    try {
      let finalImageUrl = isEdit ? initialImageUrl : null
      if (file) {
        setUploading(true)
        const data = await uploadImage(file)
        finalImageUrl = data.url
      }
      await onSubmit({
        title: values.title.trim(),
        description: values.description.trim(),
        price,
        stock,
        categoryId: Number(values.categoryId),
        imageUrls: finalImageUrl ? [finalImageUrl] : [],
      })
    } catch (err) {
      setError(err.message || 'Error al guardar el producto')
    } finally {
      setLoading(false)
      setUploading(false)
    }
  }

  const busy = loading || uploading

  return (
    <form className="product-form" onSubmit={handleSubmit}>
      {error && <p className="auth-error">{error}</p>}

      <div className="product-form-grid">
        <div className="auth-field">
          <label htmlFor="product-title">Título</label>
          <input
            id="product-title"
            name="title"
            maxLength={200}
            value={values.title}
            onChange={handleChange}
            placeholder="Ej: Mouse inalámbrico Logitech"
          />
        </div>
        <div className="auth-field">
          <label htmlFor="product-category">Categoría</label>
          <select id="product-category" name="categoryId" value={values.categoryId} onChange={handleChange}>
            <option value="">Selecciona una categoría</option>
            {categories.map(category => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>
        <div className="auth-field">
          <label htmlFor="product-price">Precio</label>
          <input
            id="product-price"
            name="price"
            type="number"
            min="0.01"
            step="0.01"
            value={values.price}
            onChange={handleChange}
            placeholder="0.00"
          />
        </div>
        <div className="auth-field">
          <label htmlFor="product-stock">Stock</label>
          <input
            id="product-stock"
            name="stock"
            type="number"
            min="0"
            step="1"
            value={values.stock}
            onChange={handleChange}
            placeholder="0"
          />
        </div>
      </div>

      <div className="auth-field">
        <label htmlFor="product-description">Descripción</label>
        <textarea
          id="product-description"
          name="description"
          maxLength={4000}
          rows={4}
          value={values.description}
          onChange={handleChange}
          placeholder="Describe tu producto"
        />
      </div>

      <div className="auth-field">
        <label htmlFor="product-image">Imagen del producto</label>
        <input id="product-image" type="file" accept="image/*" onChange={handleFileChange} disabled={busy} />
        {previewUrl && (
          <div className="product-form-preview">
            <img src={previewUrl} alt="Vista previa del producto" />
            {file && <span>Nueva imagen lista para subir</span>}
          </div>
        )}
      </div>

      <div className="form-actions">
        <button type="submit" className="btn-primary" disabled={busy}>
          {uploading ? 'Subiendo imagen...' : loading ? 'Guardando...' : isEdit ? 'Guardar cambios' : 'Crear producto'}
        </button>
        <button type="button" className="btn-secondary" onClick={onCancel} disabled={busy}>
          Cancelar
        </button>
      </div>
    </form>
  )
}
