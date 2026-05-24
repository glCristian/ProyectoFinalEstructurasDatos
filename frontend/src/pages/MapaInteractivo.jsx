import React, { useEffect, useState } from 'react'
import MapaInteractivoComp from '../components/MapaInteractivo.jsx'
import { useAuth } from '../context/AuthContext'
import { getAtracciones, cargarDatosPrueba } from '../services/api.js'

export default function MapaInteractivo() {
  const { sesion } = useAuth()
  const [total,   setTotal]   = useState(0)
  const [activas, setActivas] = useState(0)
  const [msg,     setMsg]     = useState('')

  const esAdmin = (sesion?.rol || '').toUpperCase() === 'ADMIN'

  const cargar = async () => {
    const r = await getAtracciones()
    if (r.ok) {
      setTotal(r.data.length)
      setActivas(r.data.filter(a => a.estado === 'ACTIVA').length)
    }
  }

  useEffect(() => { cargar() }, [])

  const handleCargaPrueba = async () => {
    setMsg('Cargando…')
    const r = await cargarDatosPrueba()
    setMsg(r.ok ? '✅ Datos cargados — recarga la página para ver el grafo actualizado' : '❌ Error')
    cargar()
  }

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h1 className="page-title">🌐 MAPA INTERACTIVO</h1>
          <p className="page-subtitle">
            Grafo del parque · {total} atracciones · {activas} activas
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {msg && <span style={{ fontSize: '0.8rem', color: 'var(--color-success)' }}>{msg}</span>}
          <button className="btn btn-ghost" onClick={cargar}>↺ Refrescar stats</button>
          {total === 0 && esAdmin && (
            <button className="btn btn-secondary" onClick={handleCargaPrueba}>
              📂 Cargar datos de prueba
            </button>
          )}
        </div>
      </div>

      {/* Instrucciones */}
      <div className="card" style={{ marginBottom: '1rem', display: 'flex', gap: '2rem', flexWrap: 'wrap', fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
        <span>{esAdmin ? '🖱️ Arrastra para mover el mapa' : '👀 Solo el administrador puede mover las atracciones'}</span>
        <span>🔍 Rueda del ratón para hacer zoom</span>
        <span>🖱️ Clic sobre un nodo para seleccionarlo</span>
        <span>⚡ Usa los selectores para calcular la ruta óptima (Dijkstra)</span>
      </div>

      {total === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--color-text-muted)' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🗺️</div>
          <div style={{ marginBottom: '1rem' }}>No hay atracciones cargadas en el grafo.</div>
          {esAdmin && (
            <button className="btn btn-secondary" onClick={handleCargaPrueba}>
              📂 Cargar escenario de prueba
            </button>
          )}
        </div>
      ) : (
        <MapaInteractivoComp puedeEditarPosiciones={esAdmin} />
      )}
    </div>
  )
}
