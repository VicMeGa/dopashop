import { useState } from 'react'
import { Link } from 'react-router-dom'
import { createReview } from '../../../api/reviews.api'
import useAuth from '../../../hooks/useAuth'

export default function ReviewForm({ productId, onSuccess }) {
  const { isAuthenticated } = useAuth()
  const [rating, setRating] = useState(0)
  const [hover, setHover] = useState(0)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  if (!isAuthenticated) {
    return (
      <p className="review-login-hint">
        Inicia sesión para dejar una reseña.{' '}
        <Link to="/login" state={{ from: `/product/${productId}` }}>Iniciar sesión</Link>
      </p>
    )
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSubmitting(true)
    try {
      await createReview(productId, { rating, comment: comment.trim() || null })
      setRating(0)
      setHover(0)
      setComment('')
      setSuccess('¡Gracias por tu reseña!')
      if (onSuccess) onSuccess()
    } catch (e) {
      setError(e.status === 403
        ? 'Debes haber comprado este producto para poder comentarlo'
        : 'Error al enviar la reseña. Intenta de nuevo.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="review-form" onSubmit={handleSubmit}>
      <h4>Deja tu reseña</h4>

      <div className="review-rating-selector">
        <span className="review-form-label">Tu puntaje</span>
        <div className="review-stars review-stars--selectable">
          {[1, 2, 3, 4, 5].map(n => (
            <button
              type="button"
              key={n}
              className={`review-star-btn${n <= (hover || rating) ? ' filled' : ''}`}
              onClick={() => setRating(n)}
              onMouseEnter={() => setHover(n)}
              onMouseLeave={() => setHover(0)}
              aria-label={`${n} estrellas`}
            >
              ★
            </button>
          ))}
          {rating > 0 && <span className="review-stars-value">{rating}/5</span>}
        </div>
      </div>

      <label className="review-form-field">
        <span className="review-form-label">Comentario (opcional)</span>
        <textarea
          value={comment}
          onChange={e => setComment(e.target.value)}
          rows="4"
          maxLength={2000}
          placeholder="Contanos tu experiencia con el producto..."
        />
      </label>

      {error && <p className="auth-error">{error}</p>}
      {success && <p className="review-success">{success}</p>}

      <button className="btn-primary" type="submit" disabled={submitting || rating < 1}>
        {submitting ? 'Enviando...' : 'Enviar reseña'}
      </button>
    </form>
  )
}
