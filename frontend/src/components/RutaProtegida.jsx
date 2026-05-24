import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function RutaProtegida({ children, roles }) {
    const { sesion } = useAuth()
    if (!sesion) return <Navigate to="/login" replace />
    if (roles && !roles.includes(sesion.rol)) return <Navigate to="/login" replace />
    return children
}