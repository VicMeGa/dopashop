import { useState } from 'react'
import { validarEmail, validarPassword, validarRequerido, validarMaxLength } from '../../../utils/validators'

export default function AuthForm({ title, onSubmit, error, loading, fields, confirmPassword }) {
  const [values, setValues] = useState(() =>
    Object.fromEntries(fields.map(f => [f.name, f.default || '']))
  )
  const [confirmValue, setConfirmValue] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  function handleChange(e) {
    setValues(prev => ({ ...prev, [e.target.name]: e.target.value }))
    setFieldErrors(prev => ({ ...prev, [e.target.name]: '' }))
  }

  function validate() {
    const errs = {}
    for (const f of fields) {
      const val = values[f.name]
      if (f.name === 'email') errs.email = validarEmail(val)
      else if (f.name === 'password') errs.password = validarPassword(val, f.minLength || 8)
      else if (f.name === 'fullName') {
        errs.fullName = validarRequerido(val, 'El nombre completo')
        if (!errs.fullName) errs.fullName = validarMaxLength(val, 120, 'El nombre')
      }
    }
    if (confirmPassword) {
      if (values.password !== confirmValue) errs.confirmPassword = 'Las contraseñas no coinciden'
    }
    const filtered = Object.fromEntries(Object.entries(errs).filter(([, v]) => v))
    setFieldErrors(filtered)
    return Object.keys(filtered).length === 0
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (!validate()) return
    const payload = { ...values }
    if (confirmPassword) delete payload.confirmPassword
    onSubmit(payload)
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <h2>{title}</h2>
      {error && <p className="auth-error">{error}</p>}
      {fields.map(f => (
        <div key={f.name} className="auth-field">
          <label htmlFor={f.name}>{f.label}</label>
          <input
            id={f.name}
            name={f.name}
            type={f.type || 'text'}
            value={values[f.name]}
            onChange={handleChange}
            autoComplete={f.autoComplete}
            className={fieldErrors[f.name] ? 'field-error-input' : ''}
          />
          {fieldErrors[f.name] && <span className="field-error">{fieldErrors[f.name]}</span>}
        </div>
      ))}
      {confirmPassword && (
        <div className="auth-field">
          <label htmlFor="confirmPassword">Confirmar contraseña</label>
          <input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            value={confirmValue}
            onChange={e => { setConfirmValue(e.target.value); setFieldErrors(prev => ({ ...prev, confirmPassword: '' })) }}
            autoComplete="new-password"
            className={fieldErrors.confirmPassword ? 'field-error-input' : ''}
          />
          {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
        </div>
      )}
      <button type="submit" className="auth-submit" disabled={loading}>
        {loading ? 'Cargando...' : title}
      </button>
    </form>
  )
}