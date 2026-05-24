import React, { useEffect, useState } from 'react'
import {
  getZonas, crearZona, eliminarZona, actualizarZona,
  getAtracciones, crearAtraccion, eliminarAtraccion, actualizarAtraccion,
  getClima, postClima, desactivarAlerta,
  getAforo, cargarDatosPrueba, getClusters, actualizarCapacidadParque
} from '../services/api.js'

export default function PanelAdmin() {
  const [zonas,       setZonas]       = useState([])
  const [atracciones, setAtracciones] = useState([])
  const [clima,       setClima]       = useState(null)
  const [aforo,       setAforo]       = useState(null)
  const [msg,         setMsg]         = useState('')
  const [tab,         setTab]         = useState('zonas') // zonas | atracciones | clima | datos
  const [clusters,    setClusters]    = useState(null)
  const [clustersMsg, setClustersMsg] = useState('')
  const [loadingClusters, setLoadingClusters] = useState(false)

  const [zonaForm, setZonaForm] = useState({ id: '', nombre: '', capacidadMaxima: '' })
  const [atracForm, setAtracForm] = useState({ id:'', nombre:'', tipo:'ACUATICA', capacidadMaximaPorCiclo:'', alturaMinima:'', edadMinima:'', costoAdicional:'0', zonaId:'' })
  const [tipoAlerta, setTipoAlerta] = useState('TORMENTA_ELECTRICA')
  const [zonaEdit, setZonaEdit] = useState(null)
  const [zonaEditForm, setZonaEditForm] = useState({ id: '', nombre: '', capacidadMaxima: '' })
  const [atracEdit, setAtracEdit] = useState(null)
  const [atracEditForm, setAtracEditForm] = useState({ id:'', nombre:'', tipo:'ACUATICA', capacidadMaximaPorCiclo:'', alturaMinima:'', edadMinima:'', costoAdicional:'0', zonaId:'' })
  const [capParque, setCapParque] = useState('')

  const cargar = async () => {
    const [zr, ar, cr, afor] = await Promise.all([getZonas(), getAtracciones(), getClima(), getAforo()])
    if (zr.ok)   setZonas(zr.data)
    if (ar.ok)   setAtracciones(ar.data)
    if (cr.ok)   setClima(cr.data)
    if (afor.ok) {
      setAforo(afor.data)
      setCapParque(String(afor.data.capacidadMaxima ?? ''))
    }
  }

  useEffect(() => { cargar() }, [])

  useEffect(() => {
    const cargarClusters = async () => {
      if (tab !== 'grafo') return
      setLoadingClusters(true)
      setClustersMsg('')
      const r = await getClusters()
      if (r.ok) {
        setClusters(r.data)
      } else {
        setClustersMsg('No se pudo cargar el análisis de clústeres')
      }
      setLoadingClusters(false)
    }
    cargarClusters()
  }, [tab])

  const flash = (ok, msg) => { setMsg((ok ? '✅ ' : '❌ ') + msg); cargar() }

  const handleCrearZona = async () => {
    const r = await crearZona({ ...zonaForm, capacidadMaxima: Number(zonaForm.capacidadMaxima) })
    flash(r.ok, r.ok ? 'Zona creada' : r.data)
    if (r.ok) setZonaForm({ id: '', nombre: '', capacidadMaxima: '' })
  }

  const handleCrearAtrac = async () => {
    const r = await crearAtraccion({
      ...atracForm,
      capacidadMaximaPorCiclo: Number(atracForm.capacidadMaximaPorCiclo),
      alturaMinima: Number(atracForm.alturaMinima),
      edadMinima: Number(atracForm.edadMinima),
      costoAdicional: Number(atracForm.costoAdicional),
    })
    flash(r.ok, r.ok ? 'Atracción creada' : r.data)
    if (r.ok) setAtracForm({ id:'', nombre:'', tipo:'ACUATICA', capacidadMaximaPorCiclo:'', alturaMinima:'', edadMinima:'', costoAdicional:'0', zonaId:'' })
  }

  const handleEditarZona = (z) => {
    setZonaEdit(z)
    setZonaEditForm({ id: z.id, nombre: z.nombre, capacidadMaxima: String(z.capacidadMaxima ?? '') })
  }

  const handleActualizarZona = async () => {
    if (!zonaEdit) return
    const r = await actualizarZona(zonaEdit.id, {
      nombre: zonaEditForm.nombre,
      capacidadMaxima: Number(zonaEditForm.capacidadMaxima)
    })
    flash(r.ok, r.ok ? 'Zona actualizada' : r.data)
    if (r.ok) {
      setZonaEdit(null)
      setZonaEditForm({ id: '', nombre: '', capacidadMaxima: '' })
    }
  }

  const handleEditarAtrac = (a) => {
    setAtracEdit(a)
    setAtracEditForm({
      id: a.id,
      nombre: a.nombre,
      tipo: a.tipo,
      capacidadMaximaPorCiclo: String(a.capacidadMaximaPorCiclo ?? ''),
      alturaMinima: String(a.alturaMinima ?? ''),
      edadMinima: String(a.edadMinima ?? ''),
      costoAdicional: String(a.costoAdicional ?? '0'),
      zonaId: a.zonaId ?? ''
    })
  }

  const handleActualizarAtrac = async () => {
    if (!atracEdit) return
    const r = await actualizarAtraccion(atracEdit.id, {
      nombre: atracEditForm.nombre,
      tipo: atracEditForm.tipo,
      capacidadMaximaPorCiclo: Number(atracEditForm.capacidadMaximaPorCiclo),
      alturaMinima: Number(atracEditForm.alturaMinima),
      edadMinima: Number(atracEditForm.edadMinima),
      costoAdicional: Number(atracEditForm.costoAdicional),
      zonaId: atracEditForm.zonaId
    })
    flash(r.ok, r.ok ? 'Atracción actualizada' : r.data)
    if (r.ok) {
      setAtracEdit(null)
      setAtracEditForm({ id:'', nombre:'', tipo:'ACUATICA', capacidadMaximaPorCiclo:'', alturaMinima:'', edadMinima:'', costoAdicional:'0', zonaId:'' })
    }
  }

  const handleActivarAlerta = async () => {
    const r = await postClima(tipoAlerta)
    flash(r.ok, r.ok ? `Alerta activada. Cerradas: ${r.data.cerradas?.join(', ') || 'ninguna'}` : r.data)
  }

  const handleDesactivar = async () => {
    const r = await desactivarAlerta()
    flash(r.ok, 'Alerta desactivada')
  }

  const handleCargarPrueba = async () => {
    const r = await cargarDatosPrueba()
    flash(r.ok, r.ok ? 'Escenario de prueba cargado' : r.data)
  }

  const handleActualizarCapacidad = async () => {
    if (!capParque) return
    const r = await actualizarCapacidadParque(Number(capParque))
    flash(r.ok, r.ok ? 'Capacidad del parque actualizada' : r.data)
  }

  const tabs = [['zonas','🏙️ Zonas'],['atracciones','🎢 Atracciones'],['clima','⛈️ Clima'],['grafo','🧭 Grafo'],['datos','📂 Datos']]

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">⚙️ PANEL DE ADMINISTRACIÓN</h1>
        <p className="page-subtitle">Gestión de zonas, atracciones, clima y carga de datos</p>
      </div>

      {msg && <div style={{ marginBottom: '1rem', fontSize: '0.85rem', color: msg.startsWith('❌') ? 'var(--color-danger)' : 'var(--color-success)' }}>{msg}</div>}

      {clima?.alertaActiva && <div className="alerta-banner">⛈️ ALERTA ACTIVA: {clima.tipoAlerta}</div>}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.4rem', marginBottom: '1.2rem', flexWrap: 'wrap' }}>
        {tabs.map(([k,l]) => (
          <button key={k} className={`btn ${tab===k?'btn-primary':'btn-ghost'}`} onClick={() => setTab(k)}>{l}</button>
        ))}
      </div>

      {/* ── ZONAS ── */}
      {tab === 'zonas' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '1.2rem' }}>
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>NUEVA ZONA</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <input className="input" placeholder="ID" value={zonaForm.id} onChange={e => setZonaForm(f=>({...f,id:e.target.value}))} />
              <input className="input" placeholder="Nombre" value={zonaForm.nombre} onChange={e => setZonaForm(f=>({...f,nombre:e.target.value}))} />
              <input className="input" placeholder="Capacidad máxima" value={zonaForm.capacidadMaxima} onChange={e => setZonaForm(f=>({...f,capacidadMaxima:e.target.value}))} />
              <button className="btn btn-primary" onClick={handleCrearZona}>+ Crear zona</button>
            </div>
          </div>
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              ZONAS ({zonas.length})
            </div>
            <div className="table-wrapper">
              <table>
                <thead><tr><th>ID</th><th>Nombre</th><th>Capacidad</th><th></th></tr></thead>
                <tbody>
                  {zonas.map(z => (
                    <tr key={z.id}>
                      <td>{z.id}</td><td>{z.nombre}</td><td>{z.capacidadMaxima}</td>
                      <td style={{ display: 'flex', gap: '0.35rem' }}>
                        <button className="btn btn-ghost" style={{padding:'2px 8px',fontSize:'0.75rem'}} onClick={() => handleEditarZona(z)}>✎</button>
                        <button className="btn btn-danger" style={{padding:'2px 8px',fontSize:'0.75rem'}} onClick={async()=>{await eliminarZona(z.id);cargar()}}>✕</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {zonaEdit && (
              <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--color-border)' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.75rem', color: 'var(--color-primary)', marginBottom: '0.5rem', letterSpacing: '0.04em' }}>
                  EDITAR ZONA · {zonaEdit.id}
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                  <input className="input" value={zonaEditForm.nombre} onChange={e => setZonaEditForm(f=>({...f,nombre:e.target.value}))} placeholder="Nombre" />
                  <input className="input" value={zonaEditForm.capacidadMaxima} onChange={e => setZonaEditForm(f=>({...f,capacidadMaxima:e.target.value}))} placeholder="Capacidad" />
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.6rem' }}>
                  <button className="btn btn-primary" onClick={handleActualizarZona}>Guardar</button>
                  <button className="btn btn-ghost" onClick={() => { setZonaEdit(null); setZonaEditForm({ id: '', nombre: '', capacidadMaxima: '' }) }}>Cancelar</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── ATRACCIONES ── */}
      {tab === 'atracciones' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '1.2rem' }}>
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>NUEVA ATRACCIÓN</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {[['id','ID'],['nombre','Nombre'],['capacidadMaximaPorCiclo','Capacidad/ciclo'],['alturaMinima','Altura mín (m)'],['edadMinima','Edad mín'],['costoAdicional','Costo adicional']].map(([k,l]) => (
                <input key={k} className="input" placeholder={l} value={atracForm[k]} onChange={e=>setAtracForm(f=>({...f,[k]:e.target.value}))} />
              ))}
              <select className="select" value={atracForm.tipo} onChange={e=>setAtracForm(f=>({...f,tipo:e.target.value}))}>
                {['ACUATICA','MECANICA_ALTURA','MECANICA_SUELO','SHOW','FAMILIAR','OTRO'].map(t=><option key={t} value={t}>{t}</option>)}
              </select>
              <select className="select" value={atracForm.zonaId} onChange={e=>setAtracForm(f=>({...f,zonaId:e.target.value}))}>
                <option value="">— zona —</option>
                {zonas.map(z=><option key={z.id} value={z.id}>{z.nombre}</option>)}
              </select>
              <button className="btn btn-primary" onClick={handleCrearAtrac}>+ Crear atracción</button>
            </div>
          </div>
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              ATRACCIONES ({atracciones.length})
            </div>
            <div className="table-wrapper">
              <table>
                <thead><tr><th>ID</th><th>Nombre</th><th>Tipo</th><th>Estado</th><th>Zona</th><th></th></tr></thead>
                <tbody>
                  {atracciones.map(a => (
                    <tr key={a.id}>
                      <td>{a.id}</td><td>{a.nombre}</td><td style={{fontSize:'0.75rem'}}>{a.tipo}</td>
                      <td><span className={`badge badge-${a.estado?.toLowerCase().replace('_','-') ?? 'cerrada'}`}>{a.estado}</span></td>
                      <td>{a.zonaId}</td>
                      <td style={{ display: 'flex', gap: '0.35rem' }}>
                        <button className="btn btn-ghost" style={{padding:'2px 8px',fontSize:'0.75rem'}} onClick={() => handleEditarAtrac(a)}>✎</button>
                        <button className="btn btn-danger" style={{padding:'2px 8px',fontSize:'0.75rem'}} onClick={async()=>{await eliminarAtraccion(a.id);cargar()}}>✕</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {atracEdit && (
              <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--color-border)' }}>
                <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.75rem', color: 'var(--color-primary)', marginBottom: '0.5rem', letterSpacing: '0.04em' }}>
                  EDITAR ATRACCIÓN · {atracEdit.id}
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                  <input className="input" value={atracEditForm.nombre} onChange={e=>setAtracEditForm(f=>({...f,nombre:e.target.value}))} placeholder="Nombre" />
                  <select className="select" value={atracEditForm.tipo} onChange={e=>setAtracEditForm(f=>({...f,tipo:e.target.value}))}>
                    {['ACUATICA','MECANICA_ALTURA','MECANICA_SUELO','SHOW','FAMILIAR','OTRO'].map(t=><option key={t} value={t}>{t}</option>)}
                  </select>
                  <input className="input" value={atracEditForm.capacidadMaximaPorCiclo} onChange={e=>setAtracEditForm(f=>({...f,capacidadMaximaPorCiclo:e.target.value}))} placeholder="Capacidad/ciclo" />
                  <input className="input" value={atracEditForm.alturaMinima} onChange={e=>setAtracEditForm(f=>({...f,alturaMinima:e.target.value}))} placeholder="Altura mín (m)" />
                  <input className="input" value={atracEditForm.edadMinima} onChange={e=>setAtracEditForm(f=>({...f,edadMinima:e.target.value}))} placeholder="Edad mín" />
                  <input className="input" value={atracEditForm.costoAdicional} onChange={e=>setAtracEditForm(f=>({...f,costoAdicional:e.target.value}))} placeholder="Costo adicional" />
                  <select className="select" value={atracEditForm.zonaId} onChange={e=>setAtracEditForm(f=>({...f,zonaId:e.target.value}))}>
                    <option value="">— zona —</option>
                    {zonas.map(z=><option key={z.id} value={z.id}>{z.nombre}</option>)}
                  </select>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.6rem' }}>
                  <button className="btn btn-primary" onClick={handleActualizarAtrac}>Guardar</button>
                  <button className="btn btn-ghost" onClick={() => { setAtracEdit(null); setAtracEditForm({ id:'', nombre:'', tipo:'ACUATICA', capacidadMaximaPorCiclo:'', alturaMinima:'', edadMinima:'', costoAdicional:'0', zonaId:'' }) }}>Cancelar</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── CLIMA ── */}
      {tab === 'clima' && (
        <div className="card" style={{ maxWidth: 500 }}>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '1rem', letterSpacing: '0.04em' }}>SIMULACIÓN CLIMÁTICA</div>
          <div style={{ marginBottom: '1rem' }}>
            <div style={{ fontSize: '0.85rem', marginBottom: '0.5rem' }}>Estado actual:
              <strong style={{ marginLeft: '0.5rem', color: clima?.alertaActiva ? 'var(--color-danger)' : 'var(--color-success)' }}>
                {clima?.alertaActiva ? `⛈️ ${clima.tipoAlerta}` : '☀️ Sin alerta'}
              </strong>
            </div>
          </div>
          <select className="select" style={{ width: '100%', marginBottom: '0.75rem' }} value={tipoAlerta} onChange={e => setTipoAlerta(e.target.value)}>
            <option value="TORMENTA_ELECTRICA">⚡ Tormenta eléctrica</option>
            <option value="LLUVIA_FUERTE">🌧️ Lluvia fuerte</option>
          </select>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn btn-danger" onClick={handleActivarAlerta}>⛈️ Activar alerta</button>
            <button className="btn btn-success" onClick={handleDesactivar}>☀️ Desactivar</button>
          </div>
          <div style={{ marginTop: '1rem', fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
            Al activar una alerta, las atracciones ACUÁTICA y MECÁNICA DE ALTURA se cierran automáticamente.
          </div>
        </div>
      )}

      {/* ── DATOS ── */}
      {tab === 'datos' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', maxWidth: 500 }}>
          {aforo && (
            <div className="card">
              <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem' }}>AFORO DEL PARQUE</div>
              <div style={{ fontSize: '1.8rem', fontFamily: 'var(--font-display)', color: 'var(--color-primary)' }}>
                {aforo.visitantesActuales} / {aforo.capacidadMaxima}
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.75rem' }}>
                <input className="input" value={capParque} onChange={e => setCapParque(e.target.value)} placeholder="Capacidad máxima" />
                <button className="btn btn-secondary" onClick={handleActualizarCapacidad}>Actualizar</button>
              </div>
            </div>
          )}
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem' }}>CARGA DE DATOS</div>
            <button className="btn btn-secondary" style={{ width: '100%', marginBottom: '0.5rem' }} onClick={handleCargarPrueba}>
              📂 Cargar escenario de prueba
            </button>
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
              Carga zonas, atracciones, operadores y visitantes de ejemplo para demostración.
            </div>
          </div>
        </div>
      )}

      {/* ── GRAFO ── */}
      {tab === 'grafo' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '1.2rem', maxWidth: 800 }}>
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              CLÚSTERES DEL GRAFO
            </div>
            {loadingClusters && (
              <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>Cargando…</div>
            )}
            {!loadingClusters && clustersMsg && (
              <div style={{ fontSize: '0.8rem', color: 'var(--color-danger)' }}>{clustersMsg}</div>
            )}
            {!loadingClusters && !clustersMsg && clusters && Object.keys(clusters).length === 0 && (
              <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>No hay nodos en el grafo</div>
            )}
            {!loadingClusters && !clustersMsg && clusters && Object.keys(clusters).length > 0 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {Object.entries(clusters).map(([nombre, nodos]) => (
                  <div key={nombre} style={{ background: 'var(--color-surface2)', borderRadius: 'var(--radius)', padding: '0.75rem' }}>
                    <div style={{ fontWeight: 600, marginBottom: '0.35rem' }}>{nombre}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
                      {Array.isArray(nodos) ? nodos.join(', ') : '—'}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
