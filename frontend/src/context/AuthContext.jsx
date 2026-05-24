import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)
const STORAGE_KEY = 'techpark_sesion'

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(() => {
    // Recuperar de sessionStorage para aislar cada pestaña
    const saved = sessionStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) : null
  })

  const login = (datosSesion) => {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(datosSesion))
    setSesion(datosSesion)
  }

  const logout = () => {
    sessionStorage.removeItem(STORAGE_KEY)
    setSesion(null)
  }

  return (
    <AuthContext.Provider value={{ sesion, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)