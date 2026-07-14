import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import AuthForm from './components/AuthForm'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(values) {
    setError('')
    setLoading(true)
    try {
      await register(values.email, values.password, values.fullName)
      navigate('/')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <AuthForm
        title="Crear cuenta"
        onSubmit={handleSubmit}
        error={error}
        loading={loading}
        fields={[
          { name: 'fullName', label: 'Nombre completo', autoComplete: 'name' },
          { name: 'email', label: 'Email', type: 'email', autoComplete: 'email' },
          { name: 'password', label: 'Contraseña', type: 'password', minLength: 8, autoComplete: 'new-password' },
        ]}
      />
      <p className="auth-link">
        ¿Ya tienes cuenta? <Link to="/login">Iniciar sesión</Link>
      </p>
    </div>
  )
}
