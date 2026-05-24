import React, { useEffect, useState } from 'react'
import {
  getOperadores, crearOperador, eliminarOperador,
  asignarZona, getZonas, getAtracciones, actualizarEstado, registrarRevision, getRemocionAviso
} from '../services/api.js'
import ColaComponent from '../components/ColaComponent.jsx'

export default function PanelOperadoresAdmin() {
  const [operadores,  setOperadores]  = useState([])
  const [zonas,       setZonas]       = useState([])
  const [atracciones, setAtracciones] = useState([])
  const [selOp,       setSelOp]       = useState(null)
  const [msg,         setMsg]         = useState('')

  const [form, setForm] = useState({ id: '', nombre: '', documento: '', username: '', password: '', zonaId: '' })
  const [zonaAsignar, setZonaAsignar] = useState('')

  const cargar = async () => {
    const [or, zr, ar] = await Promise.all([getOperadores(), getZonas(), getAtracciones()])
    if (or.ok) setOperadores(or.data)
    if (zr.ok) setZonas(zr.data)
    if (ar.ok) setAtracciones(ar.data)
  }

  useEffect(() => { cargar() }, [])

  const handleCrear = async () => {
    if (!form.id || !form.nombre) { setMsg('❌ ID y nombre requeridos'); return }
    const r = await crearOperador(form)
    setMsg(r.ok ? '✅ Operador creado' : `❌ ${r.data}`)
    if (r.ok) { setForm({ id: '', nombre: '', documento: '', username: '', password: '', zonaId: '' }); cargar() }
  }

  const handleAsignar = async () => {
    if (!selOp || !zonaAsignar) return
    const r = await asignarZona(selOp.id, zonaAsignar)
    setMsg(r.ok ? '✅ Zona asignada' : `❌ ${r.data}`)
    cargar()
  }

  const handleEliminar = async (id) => {
    const avisoRes = await getRemocionAviso(id)
    const aviso = avisoRes.ok ? avisoRes.data?.aviso : null
    if (aviso && aviso !== 'OK') {
      const continuar = window.confirm(`${aviso}. ¿Deseas continuar?`)
      if (!continuar) return
    }

    const r = await eliminarOperador(id)
    setMsg(r.ok ? '✅ Operador eliminado' : '❌ Error')
    if (selOp?.id === id) setSelOp(null)
    cargar()
  }

  // Atracciones de la zona del operador seleccionado
  const atraccionesZona = selOp?.zonaAsignadaId
    ? atracciones.filter(a => a.zonaId === selOp.zonaAsignadaId)
    : []

  const handleCambiarEstado = async (atracId, estado) => {
    const r = await actualizarEstado(atracId, estado, estado === 'CERRADA' ? 'Cerrada por operador' : '')
    setMsg(r.ok ? `✅ Estado actualizado a ${estado}` : `❌ ${r.data}`)
    cargar()
  }

  const handleRevision = async (atracId) => {
    const r = await registrarRevision(atracId)
    setMsg(r.ok ? '✅ Revisión técnica registrada' : `❌ ${r.data}`)
    cargar()
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🔧 PANEL DE OPERADORES</h1>
        <p className="page-subtitle">Gestión de operadores, zonas y control de acceso</p>
      </div>

      {msg && <div style={{ marginBottom: '1rem', fontSize: '0.85rem', color: msg.startsWith('❌') ? 'var(--color-danger)' : 'var(--color-success)' }}>{msg}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '1.2rem' }}>

        {/* Izquierda: crear + lista */}
        <div>
          <div className="card" style={{ marginBottom: '1rem' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>NUEVO OPERADOR</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {[["id","ID"],["nombre","Nombre"],["documento","Documento"],["username","Usuario"],["password","Contraseña"]].map(([k,l]) => (
                <input
                  key={k}
                  className="input"
                  placeholder={l}
                  type={k === 'password' ? 'password' : 'text'}
                  value={form[k]}
                  onChange={e => setForm(f => ({ ...f, [k]: e.target.value }))}
                />
              ))}
              <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                Si no defines usuario/contraseña, se usarán los datos del documento.
              </div>
              <select className="select" value={form.zonaId} onChange={e => setForm(f => ({ ...f, zonaId: e.target.value }))}>
                <option value="">— zona (opcional) —</option>
                {zonas.map(z => <option key={z.id} value={z.id}>{z.nombre}</option>)}
              </select>
              <button className="btn btn-primary" onClick={handleCrear}>+ Crear</button>
            </div>
          </div>

          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              OPERADORES ({operadores.length})
            </div>
            {operadores.map(op => (
              <div key={op.id} onClick={() => setSelOp(op)}
                style={{ padding: '0.6rem 0.75rem', marginBottom: '0.3rem', borderRadius: 'var(--radius)', cursor: 'pointer',
                  background: selOp?.id === op.id ? 'rgba(0,229,255,0.08)' : 'var(--color-surface2)',
                  border: selOp?.id === op.id ? '1px solid var(--color-primary)' : '1px solid transparent',
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{op.nombre}</div>
                  <div style={{ fontSize: '0.72rem', color: 'var(--color-text-muted)' }}>
                    Zona: {op.zonaAsignadaId ? zonas.find(z=>z.id===op.zonaAsignadaId)?.nombre ?? op.zonaAsignadaId : '—'}
                  </div>
                </div>
                <button className="btn btn-danger" style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                  onClick={e => { e.stopPropagation(); handleEliminar(op.id) }}>✕</button>
              </div>
            ))}
          </div>
        </div>

        {/* Derecha: detalle y control */}
        <div>
          {!selOp ? (
            <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
              Selecciona un operador
            </div>
          ) : (
            <>
              {/* Asignar zona */}
              <div className="card" style={{ marginBottom: '1rem' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
                  ASIGNAR ZONA · {selOp.nombre}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <select className="select" style={{ flex: 1 }} value={zonaAsignar} onChange={e => setZonaAsignar(e.target.value)}>
                    <option value="">— zona —</option>
                    {zonas.map(z => <option key={z.id} value={z.id}>{z.nombre}</option>)}
                  </select>
                  <button className="btn btn-secondary" onClick={handleAsignar}>Asignar</button>
                </div>
              </div>

              {/* Colas y control */}
              {atraccionesZona.length > 0 && (
                <div className="card" style={{ marginBottom: '1rem' }}>
                  <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
                    ATRACCIONES DE {zonas.find(z=>z.id===selOp.zonaAsignadaId)?.nombre ?? '—'}
                  </div>
                  {atraccionesZona.map(a => (
                    <div key={a.id} style={{ marginBottom: '1rem', padding: '0.75rem', background: 'var(--color-surface2)', borderRadius: 'var(--radius)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                        <div>
                          <strong>{a.nombre}</strong>
                          <span className={`badge ${a.estado==='ACTIVA'?'badge-activa':a.estado==='CERRADA'?'badge-cerrada':'badge-mantenimiento'}`}
                            style={{ marginLeft: '0.5rem' }}>{a.estado}</span>
                        </div>
                      </div>
                      <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', marginBottom: '0.5rem' }}>
                        <button className="btn btn-success" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                          onClick={() => handleCambiarEstado(a.id, 'ACTIVA')}>✅ Activar</button>
                        <button className="btn btn-danger" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                          onClick={() => handleCambiarEstado(a.id, 'CERRADA')}>🔴 Cerrar</button>
                        <button className="btn btn-ghost" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                          onClick={() => handleRevision(a.id)}>🔧 Revisión</button>
                      </div>
                      <ColaComponent atraccionId={a.id} autoRefresh={8000} />
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}
