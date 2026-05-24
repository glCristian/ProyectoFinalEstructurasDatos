import React, { useEffect, useState } from 'react'
import { getAtracciones, getZonas, getClima, getAforo, cargarDatosPrueba } from '../services/api.js'
import AtraccionCard from '../components/AtraccionCard.jsx'
import ZonaCard      from '../components/ZonaCard.jsx'

export default function Inicio() {
  const [atracciones, setAtracciones] = useState([])
  const [zonas,       setZonas]       = useState([])
  const [clima,       setClima]       = useState(null)
  const [aforo,       setAforo]       = useState(null)
  const [loading,     setLoading]     = useState(true)
  const [msgCarga,    setMsgCarga]    = useState('')

  const cargar = async () => {
    setLoading(true)
    const [aRes, zRes, cRes, afoRes] = await Promise.all([
      getAtracciones(), getZonas(), getClima(), getAforo()
    ])
    if (aRes.ok)   setAtracciones(aRes.data)
    if (zRes.ok)   setZonas(zRes.data)
    if (cRes.ok)   setClima(cRes.data)
    if (afoRes.ok) setAforo(afoRes.data)
    setLoading(false)
  }

  useEffect(() => { cargar() }, [])

  const handleCargarPrueba = async () => {
    setMsgCarga('Cargando…')
    const r = await cargarDatosPrueba()
    setMsgCarga(r.ok ? '✅ Escenario de prueba cargado' : '❌ Error al cargar')
    cargar()
  }

  const stats = {
    activas:     atracciones.filter(a => a.estado === 'ACTIVA').length,
    mantenimiento: atracciones.filter(a => a.estado === 'EN_MANTENIMIENTO').length,
    cerradas:    atracciones.filter(a => a.estado === 'CERRADA').length,
  }

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h1 className="page-title">🎢 TECH-PARK UQ</h1>
          <p className="page-subtitle">Panel de estado general del parque</p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {msgCarga && <span style={{ fontSize: '0.8rem', color: 'var(--color-success)' }}>{msgCarga}</span>}
          <button className="btn btn-secondary" onClick={handleCargarPrueba}>
            📂 Cargar datos de prueba
          </button>
          <button className="btn btn-ghost" onClick={cargar}>↺ Actualizar</button>
        </div>
      </div>

      {/* Alerta climática */}
      {clima?.alertaActiva && (
        <div className="alerta-banner">
          ⛈️ ALERTA CLIMÁTICA ACTIVA: {clima.tipoAlerta}
        </div>
      )}

      {/* Stats rápidas */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
        <div className="stat-card">
          <span className="stat-label">Atracciones</span>
          <span className="stat-value">{atracciones.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">✅ Activas</span>
          <span className="stat-value" style={{ color: 'var(--color-success)' }}>{stats.activas}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">🔧 Mantenimiento</span>
          <span className="stat-value" style={{ color: 'var(--color-warning)' }}>{stats.mantenimiento}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">🔴 Cerradas</span>
          <span className="stat-value" style={{ color: 'var(--color-danger)' }}>{stats.cerradas}</span>
        </div>
        {aforo && (
          <div className="stat-card">
            <span className="stat-label">👥 Aforo</span>
            <span className="stat-value" style={{ fontSize: '1.2rem' }}>
              {aforo.visitantesActuales}/{aforo.capacidadMaxima}
            </span>
          </div>
        )}
      </div>

      {/* Zonas */}
      {zonas.length > 0 && (
        <>
          <h2 style={{ fontSize: '0.85rem', fontFamily: 'var(--font-display)', color: 'var(--color-text-muted)', letterSpacing: '0.06em', marginBottom: '0.75rem' }}>
            ZONAS DEL PARQUE
          </h2>
          <div className="card-grid" style={{ marginBottom: '1.5rem' }}>
            {zonas.map(z => <ZonaCard key={z.id} zona={z} />)}
          </div>
        </>
      )}

      {/* Atracciones */}
      {loading ? (
        <div style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
          Cargando…
        </div>
      ) : atracciones.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
          No hay atracciones cargadas.
          <br /><br />
          <button className="btn btn-secondary" onClick={handleCargarPrueba}>
            📂 Cargar escenario de prueba
          </button>
        </div>
      ) : (
        <>
          <h2 style={{ fontSize: '0.85rem', fontFamily: 'var(--font-display)', color: 'var(--color-text-muted)', letterSpacing: '0.06em', marginBottom: '0.75rem' }}>
            ATRACCIONES
          </h2>
          <div className="card-grid">
            {atracciones.map(a => <AtraccionCard key={a.id} atraccion={a} />)}
          </div>
        </>
      )}
    </div>
  )
}
