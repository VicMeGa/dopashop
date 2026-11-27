import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import useAuth from '../hooks/useAuth'
import * as cartApi from '../api/cart.api'

const GUEST_KEY = 'dopashop.guestCart'

export const CartContext = createContext(null)

function normalizeItem(item) {
  const quantity = Math.max(1, Number(item.quantity) || 1)
  const product = item.product || {}
  const subtotal = Number(item.subtotal ?? (Number(product.price) || 0) * quantity)
  const available = item.available !== undefined
    ? item.available
    : product.active !== false && Number(product.stock) >= quantity
  return { product, quantity, subtotal, available }
}

function loadGuestCart() {
  try {
    const raw = localStorage.getItem(GUEST_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    return parsed
      .filter(item => item && item.product && item.product.id != null)
      .map(normalizeItem)
  } catch {
    return []
  }
}

function saveGuestCart(items) {
  try {
    localStorage.setItem(GUEST_KEY, JSON.stringify(items))
  } catch {
  }
}

function clearGuestCart() {
  try {
    localStorage.removeItem(GUEST_KEY)
  } catch {
  }
}

export function CartProvider({ children }) {
  const { isAuthenticated, user } = useAuth()
  const [items, setItems] = useState(loadGuestCart)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const syncRef = useRef({ userId: null, promise: null })

  const applyServerCart = useCallback((cart) => {
    setItems((cart?.items || []).map(normalizeItem))
  }, [])

  useEffect(() => {
    if (!isAuthenticated) {
      syncRef.current = { userId: null, promise: null }
      setItems(loadGuestCart())
      setError('')
      setLoading(false)
      return
    }

    const userId = user?.id ?? 'unknown'
    if (syncRef.current.userId === userId && syncRef.current.promise) return

    const promise = (async () => {
      setLoading(true)
      setError('')
      try {
        const guest = loadGuestCart()
        let cart
        if (guest.length > 0) {
          cart = await cartApi.mergeCart(
            guest.map(item => ({ productId: item.product.id, quantity: item.quantity }))
          )
          clearGuestCart()
        } else {
          cart = await cartApi.getCart()
        }
        applyServerCart(cart)
      } catch (e) {
        setError(e.message || 'Error al sincronizar el carrito')
      } finally {
        setLoading(false)
      }
    })()

    syncRef.current = { userId, promise }
  }, [isAuthenticated, user?.id, applyServerCart])

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setItems(loadGuestCart())
      return
    }
    setLoading(true)
    setError('')
    try {
      applyServerCart(await cartApi.getCart())
    } catch (e) {
      setError(e.message || 'Error al cargar el carrito')
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated, applyServerCart])

  const add = useCallback(async (product) => {
    if (!product || product.stock <= 0) return

    if (!isAuthenticated) {
      setItems(prev => {
        const existing = prev.find(item => item.product.id === product.id)
        let next
        if (existing) {
          if (existing.quantity >= product.stock) return prev
          next = prev.map(item =>
            item.product.id === product.id
              ? normalizeItem({ ...item, quantity: item.quantity + 1 })
              : item
          )
        } else {
          next = [...prev, normalizeItem({ product, quantity: 1 })]
        }
        saveGuestCart(next)
        return next
      })
      return
    }

    try {
      applyServerCart(await cartApi.addItem({ productId: product.id, quantity: 1 }))
      setError('')
    } catch (e) {
      setError(e.message || 'Error al agregar al carrito')
    }
  }, [isAuthenticated, applyServerCart])

  const increase = useCallback(async (productId) => {
    const current = items.find(item => item.product.id === productId)
    if (!current) return

    if (!isAuthenticated) {
      setItems(prev => {
        const next = prev.map(item => {
          if (item.product.id !== productId) return item
          if (item.quantity >= Number(item.product.stock)) return item
          return normalizeItem({ ...item, quantity: item.quantity + 1 })
        })
        saveGuestCart(next)
        return next
      })
      return
    }

    try {
      applyServerCart(await cartApi.addItem({ productId, quantity: 1 }))
      setError('')
    } catch (e) {
      setError(e.message || 'Error al actualizar el carrito')
    }
  }, [items, isAuthenticated, applyServerCart])

  const decrease = useCallback(async (productId) => {
    const current = items.find(item => item.product.id === productId)
    if (!current) return

    if (!isAuthenticated) {
      setItems(prev => {
        const target = prev.find(item => item.product.id === productId)
        if (!target) return prev
        let next
        if (target.quantity <= 1) {
          next = prev.filter(item => item.product.id !== productId)
        } else {
          next = prev.map(item =>
            item.product.id === productId
              ? normalizeItem({ ...item, quantity: item.quantity - 1 })
              : item
          )
        }
        saveGuestCart(next)
        return next
      })
      return
    }

    try {
      if (current.quantity <= 1) {
        await cartApi.removeItem(productId)
        setItems(prev => prev.filter(item => item.product.id !== productId))
      } else {
        applyServerCart(await cartApi.updateItem(productId, current.quantity - 1))
      }
      setError('')
    } catch (e) {
      setError(e.message || 'Error al actualizar el carrito')
    }
  }, [items, isAuthenticated, applyServerCart])

  const remove = useCallback(async (productId) => {
    if (!isAuthenticated) {
      setItems(prev => {
        const next = prev.filter(item => item.product.id !== productId)
        saveGuestCart(next)
        return next
      })
      return
    }
    try {
      await cartApi.removeItem(productId)
      setItems(prev => prev.filter(item => item.product.id !== productId))
      setError('')
    } catch (e) {
      setError(e.message || 'Error al quitar el producto')
    }
  }, [isAuthenticated])

  const clear = useCallback(async () => {
    if (!isAuthenticated) {
      setItems([])
      clearGuestCart()
      return
    }
    try {
      await cartApi.clearCart()
      setItems([])
      clearGuestCart()
      setError('')
    } catch (e) {
      setError(e.message || 'Error al vaciar el carrito')
    }
  }, [isAuthenticated])

  const total = useMemo(
    () => items.reduce((sum, item) => sum + Number(item.subtotal || 0), 0),
    [items]
  )
  const itemCount = useMemo(
    () => items.reduce((sum, item) => sum + item.quantity, 0),
    [items]
  )
  const hasUnavailable = useMemo(
    () => items.some(item => item.available === false),
    [items]
  )

  const value = useMemo(() => ({
    items,
    itemCount,
    total,
    hasUnavailable,
    loading,
    error,
    add,
    increase,
    decrease,
    remove,
    clear,
    refresh,
  }), [items, itemCount, total, hasUnavailable, loading, error, add, increase, decrease, remove, clear, refresh])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart debe usarse dentro de CartProvider')
  return ctx
}
