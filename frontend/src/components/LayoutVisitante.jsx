import React, { useEffect, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { clearNotificaciones, getNotificaciones } from '../services/api'

export default function LayoutVisitante() {
  const { sesion, logout } = useAuth()
  const [notificaciones, setNotificaciones] = useState([])
  const [panelOpen, setPanelOpen] = useState(false)
  const [notifError, setNotifError] = useState('')
  const [loadingNotifs, setLoadingNotifs] = useState(false)

  const visitanteId = sesion?.userId

  const cargarNotificaciones = async () => {
    if (!visitanteId) return
    setLoadingNotifs(true)
    setNotifError('')
    const r = await getNotificaciones(visitanteId)
    if (r.ok && Array.isArray(r.data)) {
      setNotificaciones(r.data)
    } else {
      setNotifError('No se pudieron cargar las notificaciones')
    }
    setLoadingNotifs(false)
  }

  const handleLogout = () => {
    logout()
  }

  const togglePanel = () => setPanelOpen(prev => !prev)

  const marcarLeidas = async () => {
    if (!visitanteId || notificaciones.length === 0) return
    const r = await clearNotificaciones(visitanteId)
    if (r.ok) setNotificaciones([])
  }

  useEffect(() => {
    if (!visitanteId) return
    cargarNotificaciones()
    const timer = setInterval(cargarNotificaciones, 10000)
    return () => clearInterval(timer)
  }, [visitanteId])

  return (
    <div className="app-shell">
      {/* ── Barra lateral de visitante ── */}
      <nav className="sidebar sidebar--visitante">
        <div className="sidebar-logo">
          <span className="logo-icon">🎢</span>
          <span className="logo-text">Tech-Park UQ</span>
        </div>

        <div className="user-info">
          <div className="user-avatar">👤</div>
          <div className="user-details">
            <p className="user-name">{sesion?.userId ? `Visitante ${sesion.userId}` : 'Visitante'}</p>
            <p className="user-role">VISITANTE</p>
          </div>
        </div>

        <div style={{ padding: '0 1.2rem', marginBottom: '1rem' }}>
          <button className="btn btn-ghost" onClick={togglePanel} style={{ width: '100%', justifyContent: 'space-between' }}>
            <span>🔔 Notificaciones</span>
            {notificaciones.length > 0 && (
              <span style={{
                background: 'var(--color-danger)',
                color: '#fff',
                borderRadius: '999px',
                padding: '0 6px',
                fontSize: '0.75rem',
                fontWeight: 700,
              }}>
                {notificaciones.length}
              </span>
            )}
          </button>

          {panelOpen && (
            <div className="card" style={{ marginTop: '0.6rem', padding: '0.75rem 0.9rem' }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.5rem' }}>Mensajes</div>
              {loadingNotifs && (
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Cargando…</div>
              )}
              {!loadingNotifs && notificaciones.length === 0 && !notifError && (
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Sin notificaciones</div>
              )}
              {notifError && (
                <div style={{ fontSize: '0.75rem', color: 'var(--color-danger)' }}>{notifError}</div>
              )}
              {!loadingNotifs && notificaciones.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                  {notificaciones.map((n, i) => (
                    <div key={`${i}-${n}`} style={{ fontSize: '0.75rem', lineHeight: 1.3 }}>
                      {n}
                    </div>
                  ))}
                </div>
              )}
              <div style={{ marginTop: '0.6rem', display: 'flex', justifyContent: 'flex-end' }}>
                <button className="btn btn-ghost" onClick={marcarLeidas} disabled={notificaciones.length === 0}>
                  Marcar como leídas
                </button>
              </div>
            </div>
          )}
        </div>

        <ul className="nav-links">
          <li><NavLink to="/visitante/dashboard" className={({ isActive }) => isActive ? 'active' : ''}>🏠 Mi Panel</NavLink></li>
          <li><NavLink to="/visitante/mapa" className={({ isActive }) => isActive ? 'active' : ''}>🌐 Mapa</NavLink></li>
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
