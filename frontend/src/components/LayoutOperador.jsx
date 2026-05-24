import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LayoutOperador() {
  const { sesion, logout } = useAuth()

  const handleLogout = () => {
    logout()
  }

  return (
    <div className="app-shell">
      {/* ── Barra lateral de operador ── */}
      <nav className="sidebar sidebar--operador">
        <div className="sidebar-logo">
          <span className="logo-icon">🎢</span>
          <span className="logo-text">Tech-Park UQ</span>
        </div>

        <div className="user-info">
          <div className="user-avatar">🔧</div>
          <div className="user-details">
            <p className="user-name">{sesion?.userId ? `Operador ${sesion.userId}` : 'Operador'}</p>
            <p className="user-role">OPERADOR</p>
          </div>
        </div>

        <ul className="nav-links">
          <li><NavLink to="/operador/dashboard" className={({ isActive }) => isActive ? 'active' : ''}>📊 Panel</NavLink></li>
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
