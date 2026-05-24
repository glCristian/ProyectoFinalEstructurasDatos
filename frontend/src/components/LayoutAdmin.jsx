import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LayoutAdmin() {
  const { sesion, logout } = useAuth()

  const handleLogout = () => {
    logout()
  }

  return (
    <div className="app-shell">
      {/* ── Barra lateral de administrador ── */}
      <nav className="sidebar sidebar--admin">
        <div className="sidebar-logo">
          <span className="logo-icon">🎢</span>
          <span className="logo-text">Tech-Park UQ</span>
        </div>

        <div className="user-info">
          <div className="user-avatar">👤</div>
          <div className="user-details">
            <p className="user-name">{sesion?.userId ? `Administrador ${sesion.userId}` : 'Administrador'}</p>
            <p className="user-role">ADMIN</p>
          </div>
        </div>

        <ul className="nav-links">
          <li><NavLink to="/admin/dashboard" className={({ isActive }) => isActive ? 'active' : ''}>⚙️ Administración</NavLink></li>
          <li><NavLink to="/admin/operadores" className={({ isActive }) => isActive ? 'active' : ''}>🔧 Operadores</NavLink></li>
          <li><NavLink to="/admin/visitantes" className={({ isActive }) => isActive ? 'active' : ''}>👥 Visitantes</NavLink></li>
          <li><NavLink to="/admin/rutas" className={({ isActive }) => isActive ? 'active' : ''}>🗺️ Rutas</NavLink></li>
          <li><NavLink to="/admin/mapa" className={({ isActive }) => isActive ? 'active' : ''}>🌐 Mapa</NavLink></li>
          <li><NavLink to="/admin/estadisticas" className={({ isActive }) => isActive ? 'active' : ''}>📈 Estadísticas</NavLink></li>
        </ul>

        <button className="logout-btn" onClick={handleLogout}>🚪 Cerrar sesión</button>
      </nav>

      {/* ── Área de contenido principal ── */}
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
