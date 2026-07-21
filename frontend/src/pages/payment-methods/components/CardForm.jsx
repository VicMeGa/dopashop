import { useState } from 'react'

function detectBrand(cardNumber) {
  if (cardNumber.startsWith('4')) return 'VISA'
  if (cardNumber.startsWith('5')) return 'MASTERCARD'
  return 'OTHER'
}

function formatCardNumber(value) {
  const digits = value.replace(/\D/g, '').slice(0, 16)
  return digits.replace(/(\d{4})(?=\d)/g, '$1 ')
}

export default function CardForm({ onSubmit, onCancel }) {
  const [cardNumber, setCardNumber] = useState('')
  const [expiryDate, setExpiryDate] = useState('')
  const [cvv, setCvv] = useState('')
  const [cardholderName, setCardholderName] = useState('')
  const [alias, setAlias] = useState('')
  const [error, setError] = useState('')

  function handleCardNumber(e) {
    setCardNumber(formatCardNumber(e.target.value))
  }

  function handleExpiry(e) {
    let val = e.target.value.replace(/\D/g, '').slice(0, 4)
    if (val.length >= 2) val = val.slice(0, 2) + '/' + val.slice(2)
    setExpiryDate(val)
  }

  function handleCvv(e) {
    setCvv(e.target.value.replace(/\D/g, '').slice(0, 4))
  }

  function handleSubmit(e) {
    e.preventDefault()

    const digits = cardNumber.replace(/\s/g, '')
    if (digits.length < 13 || digits.length > 16) {
      setError('Número de tarjeta inválido')
      return
    }
    if (expiryDate.length < 5) {
      setError('Fecha de vencimiento inválida')
      return
    }
    if (cvv.length < 3) {
      setError('CVV inválido')
      return
    }
    if (!cardholderName.trim()) {
      setError('Nombre del titular requerido')
      return
    }

    const cardLastFour = digits.slice(-4)
    const cardBrand = detectBrand(digits)
    const finalAlias = alias.trim() || `Tarjeta terminada en ${cardLastFour}`

    // IMPORTANT: Solo enviamos alias, cardLastFour y cardBrand al backend.
    // cardNumber completo, expiryDate y cvv son solo para simulación local
    // y nunca se persisten ni se envían a ningún lado.
    onSubmit({ alias: finalAlias, cardLastFour, cardBrand })
  }

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      {error && <p className="auth-error">{error}</p>}

      <div className="auth-field">
        <label htmlFor="cardholderName">Titular de la tarjeta</label>
        <input id="cardholderName" value={cardholderName} onChange={e => setCardholderName(e.target.value)} placeholder="Nombre como figura en la tarjeta" required />
      </div>

      <div className="auth-field">
        <label htmlFor="cardNumber">Número de tarjeta</label>
        <input id="cardNumber" value={cardNumber} onChange={handleCardNumber} placeholder="1234 5678 9012 3456" inputMode="numeric" required />
      </div>

      <div className="card-form-row">
        <div className="auth-field">
          <label htmlFor="expiryDate">Vencimiento</label>
          <input id="expiryDate" value={expiryDate} onChange={handleExpiry} placeholder="MM/AA" inputMode="numeric" required />
        </div>
        <div className="auth-field">
          <label htmlFor="cvv">CVV</label>
          <input id="cvv" value={cvv} onChange={handleCvv} placeholder="123" inputMode="numeric" required />
        </div>
      </div>

      <div className="auth-field">
        <label htmlFor="alias">Alias (opcional)</label>
        <input id="alias" value={alias} onChange={e => setAlias(e.target.value)} placeholder="Ej: Mi Visa principal" />
      </div>

      <div className="form-actions">
        <button type="submit" className="btn-primary">Guardar tarjeta</button>
        <button type="button" className="btn-secondary" onClick={onCancel}>Cancelar</button>
      </div>
    </form>
  )
}
