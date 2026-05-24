import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * ProtectedRoute: Valida que el usuario esté autenticado y tenga los roles requeridos
 * @param {string[]} roles - Array de roles permitidos (e.g., ['ADMIN', 'OPERADOR'])
 * @param {React.ReactNode} children - Componente a renderizar si la validación pasa
 */
export default function ProtectedRoute({ roles, children }) {
  const { sesion } = useAuth()

  // Si no hay sesión, redirigir a login
  if (!sesion) {
    return <Navigate to="/login" replace />
  }

  // Si hay sesión pero el rol no está permitido
  if (roles && !roles.includes(sesion.rol?.toUpperCase())) {
    // Redirigir al dashboard del rol actual
    return <Navigate to={`/${sesion.rol?.toLowerCase()}/dashboard`} replace />
  }

  // Si todo es válido, renderizar el componente
  return children
}
