import { useState, useEffect } from 'react'
import { getReviewsForProduct } from '../../../api/reviews.api'

function formatDate(iso) {
  const date = new Date(iso)
  const day = date.getDate()
  const month = date.toLocaleString('es-ES', { month: 'long' })
  const year = date.getFullYear()
  return `${day} de ${month}, ${year}`
}

function Stars({ rating }) {
  return (
    <span className="review-stars" aria-label={`${rating} de 5 estrellas`}>
      <span>{'★'.repeat(rating)}</span>
      <span className="review-stars-empty">{'★'.repeat(5 - rating)}</span>
    </span>
  )
}

export default function ReviewList({ productId, refreshKey = 0 }) {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    setData(null)
    setError('')
    getReviewsForProduct(productId)
      .then(res => { if (active) setData(res) })
      .catch(e => { if (active) setError(e.message) })
    return () => { active = false }
  }, [productId, refreshKey])

  if (error && !data) return <p className="auth-error">{error}</p>
  if (!data) return <p className="reviews-loading">Cargando reseñas...</p>

  const totalReviews = data.totalReviews
  const average = data.averageRating

  return (
    <section className="product-reviews">
      <h3>Reseñas</h3>

      {totalReviews === 0 ? (
        <p className="reviews-empty">
          Aún no hay reseñas para este producto. ¡Sé el primero en comentar si ya lo compraste!
        </p>
      ) : (
        <>
          <p className="reviews-summary">
            {average != null ? average.toFixed(1) : '—'} ★ · {totalReviews} {totalReviews === 1 ? 'reseña' : 'reseñas'}
          </p>
          <div className="reviews-list">
            {data.reviews.map(review => (
              <div key={review.id} className="review-item">
                <div className="review-item-head">
                  <span className="review-item-author">{review.authorName}</span>
                  <Stars rating={review.rating} />
                  <span className="review-item-date">{formatDate(review.createdAt)}</span>
                </div>
                {review.comment && <p className="review-item-comment">{review.comment}</p>}
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  )
}
