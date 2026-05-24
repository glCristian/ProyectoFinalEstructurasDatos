import React, { useEffect, useState } from 'react'
import { getAtracciones, getBFS, postConexion, actualizarConexion, eliminarConexion } from '../services/api.js'
import RutaComponent from '../components/RutaComponent.jsx'

export default function PanelRutas() {
  const [atracciones, setAtracciones] = useState([])
  const [bfsOrigen,   setBfsOrigen]   = useState('')
  const [bfsResult,   setBfsResult]   = useState([])
  const [msg,         setMsg]         = useState('')

  // Formulario nueva conexión
  const [conForm, setConForm] = useState({ idA: '', idB: '', peso: '' })

  useEffect(() => {
    getAtracciones().then(r => { if (r.ok) setAtracciones(r.data) })
  }, [])

  const handleBFS = async () => {
    if (!bfsOrigen) return
    const r = await getBFS(bfsOrigen)
    if (r.ok) setBfsResult(r.data)
    else setMsg('❌ Error en BFS')
  }

  const handleCrearConexion = async () => {
    if (!conForm.idA || !conForm.idB || !conForm.peso) { setMsg('❌ Completa todos los campos'); return }
    const r = await postConexion(conForm.idA, conForm.idB, Number(conForm.peso))
    setMsg(r.ok ? `✅ Conexión creada: ${conForm.idA} ↔ ${conForm.idB}` : `❌ ${r.data}`)
    if (r.ok) setConForm({ idA: '', idB: '', peso: '' })
  }

  const handleActualizarConexion = async () => {
    if (!conForm.idA || !conForm.idB || !conForm.peso) { setMsg('❌ Completa todos los campos'); return }
    const r = await actualizarConexion(conForm.idA, conForm.idB, Number(conForm.peso))
    setMsg(r.ok ? `✅ Conexión actualizada: ${conForm.idA} ↔ ${conForm.idB}` : `❌ ${r.data}`)
  }

  const handleEliminarConexion = async () => {
    if (!conForm.idA || !conForm.idB) { setMsg('❌ Selecciona A y B'); return }
    const r = await eliminarConexion(conForm.idA, conForm.idB)
    setMsg(r.ok ? `✅ Conexión eliminada: ${conForm.idA} ↔ ${conForm.idB}` : `❌ ${r.data}`)
  }

  const nombreDe = id => atracciones.find(a => a.id === id)?.nombre ?? id

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🗺️ PANEL DE RUTAS</h1>
        <p className="page-subtitle">Optimización de recorridos · Dijkstra y BFS</p>
      </div>

      {msg && <div style={{ marginBottom: '1rem', fontSize: '0.85rem', color: msg.startsWith('❌') ? 'var(--color-danger)' : 'var(--color-success)' }}>{msg}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '1.2rem' }}>

        {/* Dijkstra */}
        <div>
          <RutaComponent atracciones={atracciones} />
        </div>

        {/* BFS + conexión */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>

          {/* BFS */}
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              🔍 RECORRIDO BFS
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem' }}>
              <select className="select" style={{ flex: 1 }} value={bfsOrigen} onChange={e => setBfsOrigen(e.target.value)}>
                <option value="">— nodo origen —</option>
                {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
              </select>
              <button className="btn btn-secondary" onClick={handleBFS}>Explorar</button>
            </div>
            {bfsResult.length > 0 && (
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>
                  Orden de visita ({bfsResult.length} nodos):
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.3rem' }}>
                  {bfsResult.map((id, i) => (
                    <div key={id} style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                      <span style={{ background: 'var(--color-surface2)', padding: '3px 8px', borderRadius: '999px', fontSize: '0.75rem' }}>
                        {i+1}. {nombreDe(id)}
                      </span>
                      {i < bfsResult.length-1 && <span style={{ color: 'var(--color-text-muted)' }}>→</span>}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Nueva conexión */}
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              ➕ NUEVA CONEXIÓN
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <select className="select" value={conForm.idA} onChange={e => setConForm(f=>({...f,idA:e.target.value}))}>
                <option value="">— atracción A —</option>
                {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
              </select>
              <select className="select" value={conForm.idB} onChange={e => setConForm(f=>({...f,idB:e.target.value}))}>
                <option value="">— atracción B —</option>
                {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
              </select>
              <input className="input" placeholder="Distancia (metros)" value={conForm.peso} onChange={e => setConForm(f=>({...f,peso:e.target.value}))} />
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <button className="btn btn-primary" onClick={handleCrearConexion}>Conectar</button>
                <button className="btn btn-secondary" onClick={handleActualizarConexion}>Actualizar</button>
                <button className="btn btn-danger" onClick={handleEliminarConexion}>Eliminar</button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}
