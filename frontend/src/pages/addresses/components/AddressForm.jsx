import { useState } from 'react'

const emptyForm = { street: '', city: '', state: '', zipCode: '', country: '', isDefault: false }

export default function AddressForm({ initialValues, onSubmit, onCancel }) {
  const [values, setValues] = useState(initialValues || { ...emptyForm })

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    setValues(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    onSubmit(values)
  }

  return (
    <form className="address-form" onSubmit={handleSubmit}>
      <div className="address-form-grid">
        <div className="auth-field">
          <label htmlFor="street">Calle</label>
          <input id="street" name="street" value={values.street} onChange={handleChange} required />
        </div>
        <div className="auth-field">
          <label htmlFor="city">Ciudad</label>
          <input id="city" name="city" value={values.city} onChange={handleChange} required />
        </div>
        <div className="auth-field">
          <label htmlFor="state">Estado / Provincia</label>
          <input id="state" name="state" value={values.state} onChange={handleChange} required />
        </div>
        <div className="auth-field">
          <label htmlFor="zipCode">Código Postal</label>
          <input id="zipCode" name="zipCode" value={values.zipCode} onChange={handleChange} required />
        </div>
        <div className="auth-field">
          <label htmlFor="country">País</label>
          <input id="country" name="country" value={values.country} onChange={handleChange} required />
        </div>
      </div>
      <label className="checkbox-label">
        <input type="checkbox" name="isDefault" checked={values.isDefault} onChange={handleChange} />
        Dirección predeterminada
      </label>
      <div className="form-actions">
        <button type="submit" className="btn-primary">Guardar</button>
        <button type="button" className="btn-secondary" onClick={onCancel}>Cancelar</button>
      </div>
    </form>
  )
}
