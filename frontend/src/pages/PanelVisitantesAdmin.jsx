import React, { useEffect, useState } from 'react'
import {
  getVisitantes, registrarVisitante, retirarVisitante,
  postIngresarCola, getHistorial, getFavoritos,
  agregarFavorito, recargarSaldo, getAtracciones
} from '../services/api.js'

export default function PanelVisitantesAdmin() {
  const [visitantes,  setVisitantes]  = useState([])
  const [atracciones, setAtracciones] = useState([])
  const [selVis,      setSelVis]      = useState(null)
  const [historial,   setHistorial]   = useState([])
  const [favoritos,   setFavoritos]   = useState([])
  const [msg,         setMsg]         = useState('')

  // Formulario nuevo visitante
  const [form, setForm] = useState({ id:'', nombre:'', documento:'', edad:'', altura:'', saldoVirtual:'0' })

  // Cola
  const [atracCola, setAtracCola] = useState('')

  const cargar = async () => {
    const [vr, ar] = await Promise.all([getVisitantes(), getAtracciones()])
    if (vr.ok) setVisitantes(vr.data)
    if (ar.ok) setAtracciones(ar.data)
  }

  useEffect(() => { cargar() }, [])

  const seleccionar = async (v) => {
    setSelVis(v)
    setMsg('')
    const [hr, fr] = await Promise.all([getHistorial(v.id), getFavoritos(v.id)])
    if (hr.ok) setHistorial(Array.isArray(hr.data) ? hr.data : [])
    if (fr.ok) setFavoritos(Array.isArray(fr.data) ? fr.data : [])
  }

  const handleRegistrar = async () => {
    if (!form.id || !form.nombre) { setMsg('❌ ID y nombre requeridos'); return }
    const r = await registrarVisitante({ ...form, edad: Number(form.edad), altura: Number(form.altura), saldoVirtual: Number(form.saldoVirtual) })
    setMsg(r.ok ? '✅ Visitante registrado' : `❌ ${r.data}`)
    if (r.ok) { setForm({ id:'', nombre:'', documento:'', edad:'', altura:'', saldoVirtual:'0' }); cargar() }
  }

  const handleRetirar = async (id) => {
    if (!window.confirm('¿Eliminar este visitante del parque? Esta acción no se puede deshacer.')) return
    const r = await retirarVisitante(id)
    setMsg(r.ok ? '✅ Visitante eliminado' : `❌ ${r.data}`)
    if (selVis?.id === id) setSelVis(null)
    cargar()
  }

  const handleCola = async () => {
    if (!selVis || !atracCola) { setMsg('Selecciona visitante y atracción'); return }
    const r = await postIngresarCola(selVis.id, atracCola)
    setMsg(r.ok ? '✅ Ingresado a la cola' : `❌ ${r.data}`)
  }

  const handleFavorito = async (atracId) => {
    if (!selVis) return
    const r = await agregarFavorito(selVis.id, atracId)
    setMsg(r.ok ? '⭐ Favorito agregado' : `❌ ${r.data}`)
    seleccionar(selVis)
  }

  const handleRecarga = async () => {
    const monto = prompt('Monto a recargar:')
    if (!monto || isNaN(monto)) return
    const r = await recargarSaldo(selVis.id, Number(monto))
    setMsg(r.ok ? `✅ Saldo recargado. Nuevo saldo: $${r.data.saldoActual?.toLocaleString()}` : '❌ Error')
    cargar()
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">👤 PANEL DE VISITANTES</h1>
        <p className="page-subtitle">Registro, cola virtual, historial y favoritos</p>
      </div>

      {msg && <div style={{ marginBottom: '1rem', fontSize: '0.85rem', color: msg.startsWith('❌') ? 'var(--color-danger)' : 'var(--color-success)' }}>{msg}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '1.2rem', flexWrap: 'wrap' }}>

        {/* Columna izquierda: registro + lista */}
        <div>
          {/* Formulario */}
          <div className="card" style={{ marginBottom: '1rem' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              NUEVO VISITANTE
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {[["id","ID"],["nombre","Nombre"],["documento","Documento"],["edad","Edad"],["altura","Altura (m)"]].map(([k,l]) => (
                <input key={k} className="input" placeholder={l} value={form[k]} onChange={e => setForm(f => ({ ...f, [k]: e.target.value }))} />
              ))}
              <input className="input" placeholder="Saldo virtual" value={form.saldoVirtual} onChange={e => setForm(f => ({ ...f, saldoVirtual: e.target.value }))} />
              <button className="btn btn-primary" onClick={handleRegistrar}>+ Registrar</button>
            </div>
          </div>

          {/* Lista visitantes */}
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              VISITANTES ({visitantes.length})
            </div>
            {visitantes.length === 0 ? (
              <div style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>Sin visitantes registrados</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                {visitantes.map(v => (
                  <div key={v.id} onClick={() => seleccionar(v)}
                    style={{ padding: '0.6rem 0.75rem', borderRadius: 'var(--radius)', cursor: 'pointer',
                      background: selVis?.id === v.id ? 'rgba(0,229,255,0.08)' : 'var(--color-surface2)',
                      border: selVis?.id === v.id ? '1px solid var(--color-primary)' : '1px solid transparent',
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                    }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{v.nombre}</div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--color-text-muted)' }}>
                        {v.ticket?.tipo || 'Sin entrada'} · ${ v.saldoVirtual?.toLocaleString()}
                      </div>
                    </div>
                    <button className="btn btn-danger" style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                      onClick={e => { e.stopPropagation(); handleRetirar(v.id) }}>Eliminar</button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Columna derecha: detalle */}
        <div>
          {!selVis ? (
            <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
              Selecciona un visitante de la lista
            </div>
          ) : (
            <>
              {/* Info */}
              <div className="card" style={{ marginBottom: '1rem' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
                  {selVis.nombre.toUpperCase()}
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.4rem 1rem', fontSize: '0.85rem' }}>
                  {[['Doc.', selVis.documento], ['Edad', selVis.edad + ' años'], ['Altura', selVis.altura + ' m'], ['Ticket', selVis.ticket?.tipo || 'Sin entrada'], ['Saldo', '$' + selVis.saldoVirtual?.toLocaleString()]].map(([k, v]) => (
                    <div key={k}><span style={{ color: 'var(--color-text-muted)' }}>{k}: </span><strong>{v}</strong></div>
                  ))}
                </div>
                <button className="btn btn-ghost" style={{ marginTop: '0.75rem' }} onClick={handleRecarga}>💳 Recargar saldo</button>
              </div>

              {/* Cola virtual */}
              <div className="card" style={{ marginBottom: '1rem' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
                  FILA VIRTUAL
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <select className="select" style={{ flex: 1 }} value={atracCola} onChange={e => setAtracCola(e.target.value)}>
                    <option value="">— atracción —</option>
                    {atracciones.filter(a => a.estado === 'ACTIVA').map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
                  </select>
                  <button className="btn btn-primary" onClick={handleCola}>Unirse</button>
                </div>
              </div>

              {/* Historial */}
              <div className="card" style={{ marginBottom: '1rem' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.5rem', letterSpacing: '0.04em' }}>
                  HISTORIAL ({historial.length})
                </div>
                {historial.length === 0 ? (
                  <div style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>Sin visitas registradas</div>
                ) : (
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.3rem' }}>
                    {historial.map((id, i) => (
                      <span key={i} style={{ background: 'var(--color-surface2)', padding: '2px 8px', borderRadius: '999px', fontSize: '0.75rem' }}>
                        {atracciones.find(a => a.id === id)?.nombre ?? id}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              {/* Favoritos */}
              <div className="card">
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
                  ⭐ FAVORITOS
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '0.75rem' }}>
                  {favoritos.length === 0 ? (
                    <span style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>Sin favoritos</span>
                  ) : favoritos.map((id, i) => (
                    <span key={i} style={{ background: 'rgba(0,229,255,0.1)', color: 'var(--color-primary)', padding: '2px 8px', borderRadius: '999px', fontSize: '0.75rem' }}>
                      ⭐ {atracciones.find(a => a.id === id)?.nombre ?? id}
                    </span>
                  ))}
                </div>
                <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                  {atracciones.slice(0,4).map(a => (
                    <button key={a.id} className="btn btn-ghost" style={{ fontSize: '0.75rem', padding: '3px 8px' }}
                      onClick={() => handleFavorito(a.id)}>+ {a.nombre}</button>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
