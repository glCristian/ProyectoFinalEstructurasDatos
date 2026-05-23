import React, { useEffect, useRef, useState } from 'react'
import cytoscape from 'cytoscape'
import { getGrafo, getAtracciones, getRuta, guardarPosicionNodo } from '../services/api.js'

/**
 * MapaInteractivo
 * Visualiza el grafo del parque con Cytoscape.js.
 * - Verde  → ACTIVA
 * - Naranja → EN_MANTENIMIENTO
 * - Rojo    → CERRADA
 * Permite calcular y resaltar la ruta Dijkstra entre dos nodos.
 */
export default function MapaInteractivo({ puedeEditarPosiciones = false }) {
  const containerRef = useRef(null)
  const cyRef        = useRef(null)
  const [atracciones, setAtracciones] = useState([])
  const [origen,  setOrigen]  = useState('')
  const [destino, setDestino] = useState('')
  const [rutaMsg, setRutaMsg] = useState('')

  // ── Cargar datos y montar Cytoscape ───────────────────────────────
  useEffect(() => {
    async function init() {
      const [grafoRes, atracRes] = await Promise.all([getGrafo(), getAtracciones()])
      if (!grafoRes.ok || !atracRes.ok) return

      const grafo    = grafoRes.data
      const atracs   = atracRes.data
      setAtracciones(atracs)

      const atracMap = {}
      atracs.forEach(a => { atracMap[a.id] = a })

      const colorEstado = (estado) => ({
        ACTIVA:           '#10b981',
        EN_MANTENIMIENTO: '#f59e0b',
        CERRADA:          '#ef4444',
      }[estado] ?? '#64748b')

      // Nodos
      let nodes = (grafo.nodos ?? []).map(nodo => ({
        data: {
          id: nodo.id,
          label: nodo.nombre ?? atracMap[nodo.id]?.nombre ?? nodo.id,
          estado: nodo.estado ?? atracMap[nodo.id]?.estado ?? 'CERRADA',
          color: colorEstado(nodo.estado ?? atracMap[nodo.id]?.estado),
        }
        ,
        position: nodo.posicionX != null && nodo.posicionY != null
          ? { x: nodo.posicionX, y: nodo.posicionY }
          : undefined
      }))

      // Aristas
      const edges = (grafo.aristas ?? []).map((ar, i) => ({
        data: {
          id:     `e${i}`,
          source: ar.origen,
          target: ar.destino,
          label:  `${ar.peso}m`,
          peso:   ar.peso,
        }
      }))

      if (cyRef.current) { cyRef.current.destroy() }

      const tienePosiciones = nodes.length > 0 && nodes.every(n => n.position && Number.isFinite(n.position.x) && Number.isFinite(n.position.y))
      const layoutConfig = tienePosiciones
        ? { name: 'preset', fit: true, padding: 40, animate: false }
        : { name: 'cose', padding: 40, animate: false }

      const styles = [
        {
          selector: 'node',
          style: {
            'background-color':    'data(color)',
            'label':               'data(label)',
            'color':               '#e2e8f0',
            'font-size':           '11px',
            'font-family':         'Exo 2, sans-serif',
            'text-valign':         'bottom',
            'text-halign':         'center',
            'text-margin-y':       5,
            'width':               40,
            'height':              40,
            'border-width':        2,
            'border-color':        '#2a3147',
          }
        },
        {
          selector: 'edge',
          style: {
            'width':               2,
            'line-color':          '#2a3147',
            'target-arrow-color':  '#2a3147',
            'curve-style':         'bezier',
            'label':               'data(label)',
            'font-size':           '9px',
            'color':               '#64748b',
            'text-background-color': '#0d0f14',
            'text-background-opacity': 1,
            'text-background-padding': '2px',
          }
        },
        {
          selector: '.ruta-nodo',
          style: {
            'background-color': '#00e5ff',
            'border-color':     '#fff',
            'border-width':     3,
            'color':            '#000',
          }
        },
        {
          selector: '.ruta-arista',
          style: {
            'line-color':   '#00e5ff',
            'width':        4,
            'line-style':   'solid',
          }
        },
      ]

      cyRef.current = cytoscape({
        container: containerRef.current,
        elements:  [...nodes, ...edges],
        style: styles,
        layout: layoutConfig,
      })

      if (puedeEditarPosiciones) {
        cyRef.current.nodes().grabify()

        cyRef.current.on('dragfree', 'node', evt => {
          const node = evt.target
          const position = node.position()
          void guardarPosicionNodo(node.id(), position.x, position.y)
        })

        if (!tienePosiciones) {
          cyRef.current.on('layoutstop', () => {
            cyRef.current.nodes().forEach(node => {
              const position = node.position()
              void guardarPosicionNodo(node.id(), position.x, position.y)
            })
          })
        }
      } else {
        cyRef.current.nodes().ungrabify()
      }
    }
    init()
    return () => { if (cyRef.current) cyRef.current.destroy() }
  }, [])

  // ── Calcular y resaltar ruta ──────────────────────────────────────
  const mostrarRuta = async () => {
    if (!origen || !destino || !cyRef.current) return
    setRutaMsg('Calculando…')

    // Limpiar estilos anteriores
    cyRef.current.elements().removeClass('ruta-nodo ruta-arista')

    const r = await getRuta(origen, destino)
    if (!r.ok || !r.data.camino?.length) {
      setRutaMsg('❌ No existe camino entre esas atracciones.')
      return
    }

    const camino = r.data.camino
    camino.forEach(id => cyRef.current.$id(id).addClass('ruta-nodo'))

    for (let i = 0; i < camino.length - 1; i++) {
      cyRef.current.edges()
        .filter(e => (e.data('source') === camino[i]   && e.data('target') === camino[i+1]) ||
                     (e.data('source') === camino[i+1] && e.data('target') === camino[i]))
        .addClass('ruta-arista')
    }
    setRutaMsg(`✅ Ruta: ${camino.join(' → ')} | Distancia: ${r.data.distancia} m`)
  }

  const limpiarRuta = () => {
    if (cyRef.current) cyRef.current.elements().removeClass('ruta-nodo ruta-arista')
    setRutaMsg('')
  }

  return (
    <div>
      {/* Controles */}
      <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.75rem', alignItems: 'flex-end' }}>
        <div>
          <label style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'block', marginBottom: '0.3rem' }}>
            Desde
          </label>
          <select className="select" value={origen} onChange={e => setOrigen(e.target.value)}>
            <option value="">— origen —</option>
            {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </select>
        </div>
        <div>
          <label style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'block', marginBottom: '0.3rem' }}>
            Hasta
          </label>
          <select className="select" value={destino} onChange={e => setDestino(e.target.value)}>
            <option value="">— destino —</option>
            {atracciones.map(a => <option key={a.id} value={a.id}>{a.nombre}</option>)}
          </select>
        </div>
        <button className="btn btn-primary" onClick={mostrarRuta}>⚡ Ver ruta</button>
        <button className="btn btn-ghost" onClick={limpiarRuta}>✕ Limpiar</button>
      </div>

      {rutaMsg && (
        <div style={{ fontSize: '0.8rem', marginBottom: '0.75rem',
          color: rutaMsg.startsWith('✅') ? 'var(--color-success)' : 'var(--color-danger)' }}>
          {rutaMsg}
        </div>
      )}

      {/* Leyenda */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '0.5rem', fontSize: '0.75rem' }}>
        {[['#10b981','Activa'],['#f59e0b','Mantenimiento'],['#ef4444','Cerrada'],['#00e5ff','Ruta']].map(([c,l]) => (
          <div key={l} style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <div style={{ width: 10, height: 10, borderRadius: '50%', background: c }} />
            {l}
          </div>
        ))}
      </div>

      {/* Canvas Cytoscape */}
      <div ref={containerRef} style={{
        width: '100%', height: '520px',
        background: 'var(--color-surface)',
        border: '1px solid var(--color-border)',
        borderRadius: 'var(--radius)',
      }} />
    </div>
  )
}
