import React, { useState } from 'react'
import { getRuta } from '../services/api.js'

/**
 * RutaComponent
 * Permite al usuario elegir origen y destino y calcula la ruta óptima (Dijkstra).
 * Props:
 *   atracciones – lista de objetos Atraccion para los selectores
 */
export default function RutaComponent({ atracciones = [] }) {
  const [origen,   setOrigen]   = useState('')
  const [destino,  setDestino]  = useState('')
  const [resultado, setResultado] = useState(null)
  const [loading,  setLoading]  = useState(false)
  const [error,    setError]    = useState('')

  const calcular = async () => {
    if (!origen || !destino) { setError('Selecciona origen y destino'); return }
    if (origen === destino)  { setError('El origen y el destino deben ser distintos'); return }
    setLoading(true); setError(''); setResultado(null)
    const r = await getRuta(origen, destino)
    if (r.ok && r.data.camino?.length > 0) {
      setResultado(r.data)
    } else {
      setError('No existe camino entre esas atracciones.')
    }
    setLoading(false)
  }

  const nombreDe = (id) => atracciones.find(a => a.id === id)?.nombre ?? id

  return (
    <div className="card">
      <div style={{ fontWeight: 600, marginBottom: '1rem', color: 'var(--color-primary)', fontFamily: 'var(--font-display)', fontSize: '0.9rem', letterSpacing: '0.04em' }}>
        🗺️ CALCULAR RUTA ÓPTIMA
      </div>

      <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
        <div style={{ flex: 1, minWidth: 160 }}>
          <label style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'block', marginBottom: '0.3rem' }}>
            Desde
          </label>
          <select className="select" style={{ width: '100%' }} value={origen} onChange={e => setOrigen(e.target.value)}>
            <option value="">— seleccionar —</option>
            {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </select>
        </div>
        <div style={{ flex: 1, minWidth: 160 }}>
          <label style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'block', marginBottom: '0.3rem' }}>
            Hasta
          </label>
          <select className="select" style={{ width: '100%' }} value={destino} onChange={e => setDestino(e.target.value)}>
            <option value="">— seleccionar —</option>
            {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </select>
        </div>
        <div style={{ display: 'flex', alignItems: 'flex-end' }}>
          <button className="btn btn-primary" onClick={calcular} disabled={loading}>
            {loading ? '…' : '⚡ Calcular'}
          </button>
        </div>
      </div>

      {error && <div style={{ color: 'var(--color-danger)', fontSize: '0.85rem' }}>{error}</div>}

      {resultado && (
        <div style={{ marginTop: '0.75rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.6rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
              {resultado.camino.length} pasos
            </span>
            <span style={{ fontFamily: 'var(--font-display)', color: 'var(--color-accent)', fontSize: '0.85rem' }}>
              {resultado.distancia === 1.7976931348623157e+308 ? '∞' : resultado.distancia + ' m'}
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
            {resultado.camino.map((id, i) => (
              <div key={id} style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <div style={{
                  width: 24, height: 24, borderRadius: '50%',
                  background: i === 0 ? 'var(--color-success)' : i === resultado.camino.length - 1 ? 'var(--color-danger)' : 'var(--color-secondary)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '0.7rem', fontWeight: 700, color: '#fff', flexShrink: 0
                }}>
                  {i + 1}
                </div>
                <div style={{ fontSize: '0.875rem' }}>
                  <strong>{nombreDe(id)}</strong>
                  <span style={{ color: 'var(--color-text-muted)', marginLeft: '0.4rem', fontSize: '0.75rem' }}>({id})</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
