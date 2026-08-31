export function validarEmail(email) {
  if (!email || !email.trim()) return 'El email es obligatorio'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return 'El email no tiene un formato válido'
  return ''
}

export function validarPassword(password, min = 8, max = 72) {
  if (!password) return 'La contraseña es obligatoria'
  if (password.length < min) return `La contraseña debe tener al menos ${min} caracteres`
  if (password.length > max) return `La contraseña no puede superar los ${max} caracteres`
  return ''
}

export function validarRequerido(value, label) {
  if (!value || !value.toString().trim()) return `${label} es obligatorio`
  return ''
}

export function validarMaxLength(value, max, label) {
  if (value && value.length > max) return `${label} no puede superar los ${max} caracteres`
  return ''
}

export function validarCodigoPostal(value) {
  if (!value || !value.trim()) return 'El código postal es obligatorio'
  if (!/^[A-Za-z0-9\s-]{1,20}$/.test(value.trim())) return 'El código postal solo puede contener letras, números, espacios y guiones'
  return ''
}

export function validarNumeroTarjeta(value) {
  const digits = value.replace(/\D/g, '')
  if (digits.length < 13 || digits.length > 16) return 'Número de tarjeta inválido'
  let sum = 0
  let alternate = false
  for (let i = digits.length - 1; i >= 0; i--) {
    let n = parseInt(digits[i], 10)
    if (alternate) {
      n *= 2
      if (n > 9) n -= 9
    }
    sum += n
    alternate = !alternate
  }
  if (sum % 10 !== 0) return 'Número de tarjeta inválido'
  return ''
}

export function validarVencimiento(value) {
  const clean = value.replace(/\D/g, '')
  if (clean.length < 4) return 'Fecha de vencimiento inválida'
  const month = parseInt(clean.slice(0, 2), 10)
  const year = parseInt(clean.slice(2, 4), 10) + 2000
  if (month < 1 || month > 12) return 'Mes inválido'
  const now = new Date()
  const exp = new Date(year, month, 0)
  if (exp < now) return 'La tarjeta está vencida'
  return ''
}

export function validarCVV(value) {
  const digits = value.replace(/\D/g, '')
  if (digits.length < 3 || digits.length > 4) return 'CVV inválido'
  return ''
}

export function validarSelect(value, label) {
  if (!value || value === '') return `Selecciona ${label}`
  return ''
}