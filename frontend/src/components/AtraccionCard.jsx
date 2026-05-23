import React from 'react'

/**
 * AtraccionCard
 * Muestra los datos de una atracción en formato tarjeta.
 * Props:
 *   atraccion  – objeto Atraccion del backend
 *   onAccion   – función opcional (id) => void para botón de acción
 *   labelAccion – texto del botón (default "Ver detalle")
 */
export default function AtraccionCard({ atraccion, onAccion, labelAccion = 'Ver detalle' }) {
  if (!atraccion) return null

  const visitantesEnEspera = atraccion.visitantesEnEspera ?? atraccion.colaVirtual ?? null

  const estadoClass = {
    ACTIVA:           'badge-activa',
    EN_MANTENIMIENTO: 'badge-mantenimiento',
    CERRADA:          'badge-cerrada',
  }[atraccion.estado] ?? 'badge-cerrada'

  const tipoIcono = {
    ACUATICA:        '🌊',
    MECANICA_ALTURA: '🎢',
    MECANICA_SUELO:  '🚗',
    SHOW:            '🎭',
    FAMILIAR:        '👨‍👩‍👧',
    OTRO:            '🎠',
  }[atraccion.tipo] ?? '🎡'

  return (
    <div className="card atraccion-card">
      <div className="atraccion-card-header">
        <span className="atraccion-icono">{tipoIcono}</span>
        <div style={{ flex: 1 }}>
          <div className="atraccion-nombre">{atraccion.nombre}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
            {atraccion.tipo?.replace('_', ' ')} · Zona {atraccion.zonaId}
          </div>
        </div>
        <span className={`badge ${estadoClass}`}>{atraccion.estado?.replace('_', ' ')}</span>
      </div>

      <div className="atraccion-card-body">
        <div className="atraccion-stat">
          <span>👥 Capacidad</span>
          <strong>{atraccion.capacidadMaximaPorCiclo}</strong>
        </div>
        <div className="atraccion-stat">
          <span>📏 Altura mín.</span>
          <strong>{atraccion.alturaMinima > 0 ? `${atraccion.alturaMinima} m` : '—'}</strong>
        </div>
        <div className="atraccion-stat">
          <span>🔢 Visitantes</span>
          <strong>{visitantesEnEspera != null ? visitantesEnEspera : (atraccion.visitantesAcumulados ?? 0)}</strong>
        </div>
        <div className="atraccion-stat">
          <span>⏱️ Espera</span>
          <strong>{atraccion.tiempoEsperaEstimado > 0 ? `${atraccion.tiempoEsperaEstimado} min` : '—'}</strong>
        </div>
      </div>

      {atraccion.motivoCierre && (
        <div style={{ fontSize: '0.75rem', color: 'var(--color-danger)', marginTop: '0.5rem' }}>
          ⚠️ {atraccion.motivoCierre}
        </div>
      )}

      {onAccion && (
        <button className="btn btn-ghost" style={{ marginTop: '0.75rem', width: '100%' }}
          onClick={() => onAccion(atraccion.id)}>
          {labelAccion}
        </button>
      )}
    </div>
  )
}
