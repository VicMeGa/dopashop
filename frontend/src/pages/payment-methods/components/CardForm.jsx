import { useState } from 'react'
import { validarNumeroTarjeta, validarVencimiento, validarCVV, validarRequerido, validarMaxLength } from '../../../utils/validators'

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
  const [errors, setErrors] = useState({})

  function handleCardNumber(e) {
    setCardNumber(formatCardNumber(e.target.value))
    if (errors.cardNumber) setErrors(prev => ({ ...prev, cardNumber: '' }))
  }

  function handleExpiry(e) {
    let val = e.target.value.replace(/\D/g, '').slice(0, 4)
    if (val.length >= 2) val = val.slice(0, 2) + '/' + val.slice(2)
    setExpiryDate(val)
    if (errors.expiryDate) setErrors(prev => ({ ...prev, expiryDate: '' }))
  }

  function handleCvv(e) {
    setCvv(e.target.value.replace(/\D/g, '').slice(0, 4))
    if (errors.cvv) setErrors(prev => ({ ...prev, cvv: '' }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    setError('')

    const errs = {}
    errs.cardNumber = validarNumeroTarjeta(cardNumber)
    errs.expiryDate = validarVencimiento(expiryDate)
    errs.cvv = validarCVV(cvv)
    errs.cardholderName = validarRequerido(cardholderName, 'El nombre del titular')
    if (!errs.cardholderName) errs.cardholderName = validarMaxLength(cardholderName, 120, 'El nombre del titular')

    const filtered = Object.fromEntries(Object.entries(errs).filter(([, v]) => v))
    if (Object.keys(filtered).length > 0) {
      setErrors(filtered)
      return
    }

    const digits = cardNumber.replace(/\s/g, '')
    const cardLastFour = digits.slice(-4)
    const cardBrand = detectBrand(digits)
    const finalAlias = alias.trim() || `Tarjeta terminada en ${cardLastFour}`

    onSubmit({ alias: finalAlias, cardLastFour, cardBrand })
  }

  const errClass = name => errors[name] ? 'field-error-input' : ''

  return (
    <form className="card-form" onSubmit={handleSubmit} noValidate>
      {error && <p className="auth-error">{error}</p>}

      <div className="auth-field">
        <label htmlFor="cardholderName">Titular de la tarjeta</label>
        <input id="cardholderName" value={cardholderName} onChange={e => setCardholderName(e.target.value)} placeholder="Nombre como figura en la tarjeta" className={errClass('cardholderName')} />
        {errors.cardholderName && <span className="field-error">{errors.cardholderName}</span>}
      </div>

      <div className="auth-field">
        <label htmlFor="cardNumber">Número de tarjeta</label>
        <input id="cardNumber" value={cardNumber} onChange={handleCardNumber} placeholder="1234 5678 9012 3456" inputMode="numeric" className={errClass('cardNumber')} />
        {errors.cardNumber && <span className="field-error">{errors.cardNumber}</span>}
      </div>

      <div className="card-form-row">
        <div className="auth-field">
          <label htmlFor="expiryDate">Vencimiento</label>
          <input id="expiryDate" value={expiryDate} onChange={handleExpiry} placeholder="MM/AA" inputMode="numeric" className={errClass('expiryDate')} />
          {errors.expiryDate && <span className="field-error">{errors.expiryDate}</span>}
        </div>
        <div className="auth-field">
          <label htmlFor="cvv">CVV</label>
          <input id="cvv" value={cvv} onChange={handleCvv} placeholder="123" inputMode="numeric" className={errClass('cvv')} />
          {errors.cvv && <span className="field-error">{errors.cvv}</span>}
        </div>
      </div>

      <div className="auth-field">
        <label htmlFor="alias">Alias (opcional)</label>
        <input id="alias" value={alias} onChange={e => { setAlias(e.target.value); setError('') }} placeholder="Ej: Mi Visa principal" />
      </div>

      <div className="form-actions">
        <button type="submit" className="btn-primary">Guardar tarjeta</button>
        <button type="button" className="btn-secondary" onClick={onCancel}>Cancelar</button>
      </div>
    </form>
  )
}