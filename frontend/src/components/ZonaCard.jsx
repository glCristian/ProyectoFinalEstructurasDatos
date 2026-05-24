import React from 'react'

/**
 * ZonaCard
 * Props:
 *   zona – objeto Zona del backend
 */
export default function ZonaCard({ zona }) {
  if (!zona) return null
  const pct = zona.capacidadMaxima > 0
    ? Math.round((zona.visitantesActuales / zona.capacidadMaxima) * 100)
    : 0
  const barColor = pct > 85 ? 'var(--color-danger)' : pct > 60 ? 'var(--color-warning)' : 'var(--color-success)'

  return (
    <div className="card zona-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
        <div>
          <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{zona.nombre}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>ID: {zona.id}</div>
        </div>
        <div style={{ textAlign: 'right', fontSize: '0.85rem' }}>
          <div style={{ color: barColor, fontWeight: 700 }}>
            {zona.visitantesActuales} / {zona.capacidadMaxima}
          </div>
          <div style={{ fontSize: '0.72rem', color: 'var(--color-text-muted)' }}>visitantes</div>
        </div>
      </div>

      {/* barra de aforo */}
      <div style={{ background: 'var(--color-surface2)', borderRadius: '4px', height: '6px', overflow: 'hidden' }}>
        <div style={{
          width: `${pct}%`, height: '100%',
          background: barColor,
          transition: 'width 0.4s ease',
          borderRadius: '4px',
        }} />
      </div>
      <div style={{ fontSize: '0.72rem', color: 'var(--color-text-muted)', marginTop: '0.3rem' }}>
        {pct}% de aforo
      </div>
    </div>
  )
}
