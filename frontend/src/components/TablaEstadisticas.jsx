import React from 'react'

/**
 * TablaEstadisticas
 * Tabla genérica de estadísticas con columnas y filas dinámicas.
 * Props:
 *   titulo   – string título de la tabla
 *   columnas – array de { key, label, render? }
 *   filas    – array de objetos con los datos
 *   emptyMsg – mensaje si no hay filas
 */
export default function TablaEstadisticas({ titulo, columnas = [], filas = [], emptyMsg = 'Sin datos' }) {
  return (
    <div className="card" style={{ marginBottom: '1.2rem' }}>
      {titulo && (
        <div style={{ fontWeight: 600, marginBottom: '0.9rem', color: 'var(--color-primary)',
          fontFamily: 'var(--font-display)', fontSize: '0.85rem', letterSpacing: '0.04em' }}>
          {titulo}
        </div>
      )}

      {filas.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem', padding: '0.5rem 0' }}>
          {emptyMsg}
        </div>
      ) : (
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                {columnas.map(c => <th key={c.key}>{c.label}</th>)}
              </tr>
            </thead>
            <tbody>
              {filas.map((fila, i) => (
                <tr key={fila.id ?? i}>
                  {columnas.map(c => (
                    <td key={c.key}>
                      {c.render ? c.render(fila[c.key], fila) : (fila[c.key] ?? '—')}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
