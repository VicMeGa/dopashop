import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import AuthForm from './components/AuthForm'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const from = location.state?.from || '/'

  async function handleSubmit(values) {
    setError('')
    setLoading(true)
    try {
      await login(values.email, values.password)
      navigate(from, { replace: true })
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <AuthForm
        title="Iniciar sesión"
        onSubmit={handleSubmit}
        error={error}
        loading={loading}
        fields={[
          { name: 'email', label: 'Email', type: 'email', autoComplete: 'email' },
          { name: 'password', label: 'Contraseña', type: 'password', autoComplete: 'current-password' },
        ]}
      />
      <p className="auth-link">
        ¿No tienes cuenta? <Link to="/register">Registrarse</Link>
      </p>
    </div>
  )
}
