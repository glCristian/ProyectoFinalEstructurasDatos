import React, { useState, useEffect } from 'react'
import { getTamanioCola, procesarSiguiente } from '../services/api.js'

/**
 * ColaComponent
 * Muestra el tamaño de la cola de una atracción y permite
 * al operador procesar el siguiente visitante.
 * Props:
 *   atraccionId – ID de la atracción
 *   autoRefresh – intervalo de refresco en ms (default 5000, 0 = off)
 */
export default function ColaComponent({ atraccionId, autoRefresh = 5000 }) {
  const [tamanio, setTamanio] = useState(null)
  const [ultimoLote,  setUltimoLote]  = useState([])
  const [loading, setLoading] = useState(false)
  const [msg,     setMsg]     = useState('')

  const fetchTamanio = async () => {
    if (!atraccionId) return
    const r = await getTamanioCola(atraccionId)
    if (r.ok) setTamanio(r.data.tamanioCola ?? 0)
  }

  useEffect(() => {
    fetchTamanio()
    if (autoRefresh > 0) {
      const id = setInterval(fetchTamanio, autoRefresh)
      return () => clearInterval(id)
    }
  }, [atraccionId])

  const handleProcesar = async () => {
    setLoading(true); setMsg('')
    const r = await procesarSiguiente(atraccionId)
    if (r.status === 204) {
      setMsg('La cola está vacía.')
    } else if (r.ok) {
      const lote = Array.isArray(r.data?.procesados)
        ? r.data.procesados
        : Array.isArray(r.data)
          ? r.data
          : r.data
            ? [r.data]
            : []
      setUltimoLote(lote)
      const cantidad = r.data?.cantidadProcesados ?? lote.length
      setMsg(`✅ Procesados: ${cantidad}`)
    } else {
      setMsg(`❌ ${typeof r.data === 'string' ? r.data : 'Error al procesar'}`)
    }
    await fetchTamanio()
    setLoading(false)
  }

  return (
    <div className="card" style={{ minWidth: 220 }}>
      <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginBottom: '0.4rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
        Cola · {atraccionId}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.75rem' }}>
        <div style={{ fontFamily: 'var(--font-display)', fontSize: '2.2rem', color: 'var(--color-primary)' }}>
          {tamanio ?? '—'}
        </div>
        <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
          {tamanio === 1 ? 'visitante esperando' : 'visitantes esperando'}
        </div>
      </div>

      <button className="btn btn-primary" style={{ width: '100%' }}
        onClick={handleProcesar} disabled={loading || tamanio === 0}>
        {loading ? '…' : '▶ Procesar ciclo'}
      </button>

      {msg && (
        <div style={{ marginTop: '0.5rem', fontSize: '0.8rem',
          color: msg.startsWith('✅') ? 'var(--color-success)' : 'var(--color-text-muted)' }}>
          {msg}
        </div>
      )}

      {ultimoLote.length > 0 && (
        <div style={{ marginTop: '0.5rem', background: 'var(--color-surface2)', borderRadius: 'var(--radius)', padding: '0.5rem 0.75rem', fontSize: '0.8rem' }}>
          <strong>Último ciclo</strong>
          <div style={{ color: 'var(--color-text-muted)', marginTop: '0.25rem' }}>
            {ultimoLote.map(v => v?.nombre ?? v).join(', ')}
          </div>
        </div>
      )}
    </div>
  )
}
