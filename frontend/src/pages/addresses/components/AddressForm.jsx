import { useState } from 'react'
import { validarRequerido, validarMaxLength, validarCodigoPostal } from '../../../utils/validators'

const emptyForm = { street: '', city: '', state: '', zipCode: '', country: '', isDefault: false }

export default function AddressForm({ initialValues, onSubmit, onCancel }) {
  const [values, setValues] = useState(initialValues || { ...emptyForm })
  const [errors, setErrors] = useState({})

  function handleChange(e) {
    const { name, value, type, checked } = e.target
    setValues(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }))
  }

  function validate() {
    const errs = {}
    errs.street = validarMaxLength(values.street, 200, 'La calle') || validarRequerido(values.street, 'La calle')
    errs.city = validarMaxLength(values.city, 200, 'La ciudad') || validarRequerido(values.city, 'La ciudad')
    errs.state = validarMaxLength(values.state, 200, 'El estado/provincia') || validarRequerido(values.state, 'El estado/provincia')
    errs.zipCode = validarCodigoPostal(values.zipCode)
    errs.country = validarMaxLength(values.country, 200, 'El país') || validarRequerido(values.country, 'El país')
    const filtered = Object.fromEntries(Object.entries(errs).filter(([, v]) => v))
    setErrors(filtered)
    return Object.keys(filtered).length === 0
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (!validate()) return
    const clean = {
      street: values.street.trim(),
      city: values.city.trim(),
      state: values.state.trim(),
      zipCode: values.zipCode.trim(),
      country: values.country.trim(),
      isDefault: values.isDefault,
    }
    onSubmit(clean)
  }

  const field = (name, label) => (
    <div className="auth-field">
      <label htmlFor={name}>{label}</label>
      <input id={name} name={name} value={values[name]} onChange={handleChange} className={errors[name] ? 'field-error-input' : ''} />
      {errors[name] && <span className="field-error">{errors[name]}</span>}
    </div>
  )

  return (
    <form className="address-form" onSubmit={handleSubmit} noValidate>
      <div className="address-form-grid">
        {field('street', 'Calle')}
        {field('city', 'Ciudad')}
        {field('state', 'Estado / Provincia')}
        {field('zipCode', 'Código Postal')}
        {field('country', 'País')}
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