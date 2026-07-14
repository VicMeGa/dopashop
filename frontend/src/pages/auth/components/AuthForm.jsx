import { useState } from 'react'

export default function AuthForm({ title, onSubmit, error, loading, fields }) {
  const [values, setValues] = useState(() =>
    Object.fromEntries(fields.map(f => [f.name, f.default || '']))
  )

  function handleChange(e) {
    setValues(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    onSubmit(values)
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
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
            required={f.required !== false}
            minLength={f.minLength}
            autoComplete={f.autoComplete}
          />
        </div>
      ))}
      <button type="submit" className="auth-submit" disabled={loading}>
        {loading ? 'Cargando...' : title}
      </button>
    </form>
  )
}
