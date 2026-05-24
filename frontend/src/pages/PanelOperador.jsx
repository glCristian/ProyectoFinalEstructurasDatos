import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  getOperador, getZona, getAtracciones,
  actualizarEstado, registrarRevision
} from '../services/api.js'
import ColaComponent from '../components/ColaComponent.jsx'

export default function PanelOperador() {
  const { sesion } = useAuth()
  const [operador, setOperador] = useState(null)
  const [zona, setZona] = useState(null)
  const [atracciones, setAtracciones] = useState([])
  const [msg, setMsg] = useState('')
  const [loading, setLoading] = useState(true)

  const cargar = async () => {
    if (!sesion?.userId) {
      setLoading(false)
      return
    }

    setLoading(true)
    setMsg('')
    const opRes = await getOperador(sesion.userId)
    if (!opRes.ok) {
      setMsg('❌ No se pudo cargar el operador')
      setLoading(false)
      return
    }

    setOperador(opRes.data)
    const zonaId = opRes.data?.zonaAsignadaId
    if (!zonaId) {
      setZona(null)
      setAtracciones([])
      setLoading(false)
      return
    }

    const [zr, ar] = await Promise.all([getZona(zonaId), getAtracciones()])
    if (zr.ok) setZona(zr.data)
    if (ar.ok) setAtracciones(ar.data.filter(a => a.zonaId === zonaId))
    setLoading(false)
  }

  useEffect(() => { cargar() }, [sesion?.userId])

  const flash = (ok, okMsg, errMsg) => {
    setMsg(ok ? `✅ ${okMsg}` : `❌ ${errMsg}`)
  }

  const badgeClass = (estado) => {
    if (estado === 'ACTIVA') return 'badge-activa'
    if (estado === 'CERRADA') return 'badge-cerrada'
    if (estado === 'EN_MANTENIMIENTO') return 'badge-mantenimiento'
    return 'badge-cerrada'
  }

  const handleCambiarEstado = async (atracId, estado) => {
    const motivos = {
      CERRADA: 'Cerrada por operador',
      EN_MANTENIMIENTO: 'Mantenimiento manual por operador'
    }
    const r = await actualizarEstado(atracId, estado, motivos[estado] || '')
    flash(r.ok, `Estado actualizado a ${estado}`, r.data || 'Error al actualizar')
    if (r.ok) cargar()
  }

  const handleRevision = async (atracId) => {
    const r = await registrarRevision(atracId)
    flash(r.ok, 'Revisión técnica registrada', r.data || 'Error al registrar revisión')
    if (r.ok) cargar()
  }

  const zonaNombre = zona?.nombre || operador?.zonaAsignadaId || '—'

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🔧 PANEL DEL OPERADOR</h1>
        <p className="page-subtitle">Gestión de atracciones y colas de tu zona asignada</p>
      </div>

      {msg && (
        <div style={{ marginBottom: '1rem', fontSize: '0.85rem', color: msg.startsWith('❌') ? 'var(--color-danger)' : 'var(--color-success)' }}>
          {msg}
        </div>
      )}

      {loading && (
        <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
          Cargando panel del operador…
        </div>
      )}

      {!loading && !operador && (
        <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
          No se encontró información del operador.
        </div>
      )}

      {!loading && operador && !operador.zonaAsignadaId && (
        <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
          Este operador no tiene una zona asignada. Solicita la asignación a un administrador.
        </div>
      )}

      {!loading && operador && operador.zonaAsignadaId && (
        <>
          <div className="card" style={{ marginBottom: '1rem' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              MI ZONA
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.4rem 1rem', fontSize: '0.85rem' }}>
              <div><span style={{ color: 'var(--color-text-muted)' }}>Operador: </span><strong>{operador.nombre || operador.id}</strong></div>
              <div><span style={{ color: 'var(--color-text-muted)' }}>Zona: </span><strong>{zonaNombre}</strong></div>
              <div><span style={{ color: 'var(--color-text-muted)' }}>Atracciones: </span><strong>{atracciones.length}</strong></div>
            </div>
            <div style={{ marginTop: '0.6rem', fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
              Prioridad de cola: Fast Pass (1) → General/Familiar (2)
            </div>
          </div>

          {atracciones.length === 0 ? (
            <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
              No hay atracciones registradas en esta zona.
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem' }}>
              {atracciones.map(a => (
                <div key={a.id} className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ fontWeight: 600 }}>{a.nombre}</div>
                    <span className={`badge ${badgeClass(a.estado)}`}>{a.estado}</span>
                  </div>

                  <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                    Capacidad/ciclo: {a.capacidadMaximaPorCiclo ?? '—'} · Ciclos: {a.visitantesAcumulados ?? 0} / 500
                  </div>

                  {a.motivoCierre && a.estado !== 'ACTIVA' && (
                    <div style={{ fontSize: '0.75rem', color: 'var(--color-warning)' }}>
                      {a.motivoCierre}
                    </div>
                  )}

                  <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                    <button className="btn btn-success" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                      onClick={() => handleCambiarEstado(a.id, 'ACTIVA')}>✅ Activar</button>
                    <button className="btn btn-danger" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                      onClick={() => handleCambiarEstado(a.id, 'CERRADA')}>🔴 Cerrar</button>
                    <button className="btn btn-ghost" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                      onClick={() => handleCambiarEstado(a.id, 'EN_MANTENIMIENTO')}>🛠️ Mantenimiento</button>
                    <button className="btn btn-ghost" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                      onClick={() => handleRevision(a.id)} disabled={a.estado !== 'EN_MANTENIMIENTO'}>
                      🔧 Revisión técnica
                    </button>
                  </div>

                  <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                    Procesa el siguiente ciclo respetando la prioridad de la cola.
                  </div>
                  <ColaComponent atraccionId={a.id} autoRefresh={8000} />
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
