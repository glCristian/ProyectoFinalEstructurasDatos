import React, { useEffect, useState } from 'react'
import {
  getTopAtracciones, getMantenimiento, getCierresClima,
  getIngresoDiario, getTiempoEspera, getAforo, getAtracciones
} from '../services/api.js'
import TablaEstadisticas from '../components/TablaEstadisticas.jsx'

export default function Estadisticas() {
  const [top,         setTop]         = useState([])
  const [mantenimiento, setMant]      = useState([])
  const [cierresClima, setCierres]    = useState([])
  const [ingreso,     setIngreso]     = useState(null)
  const [espera,      setEspera]      = useState(null)
  const [aforo,       setAforo]       = useState(null)
  const [todas,       setTodas]       = useState([])
  const [loading,     setLoading]     = useState(true)

  const cargar = async () => {
    setLoading(true)
    const [tr, mr, cr, ir, er, afor, alr] = await Promise.all([
      getTopAtracciones(8),
      getMantenimiento(),
      getCierresClima(),
      getIngresoDiario(),
      getTiempoEspera(),
      getAforo(),
      getAtracciones(),
    ])
    if (tr.ok)   setTop(tr.data)
    if (mr.ok)   setMant(mr.data)
    if (cr.ok)   setCierres(Array.isArray(cr.data) ? cr.data : [])
    if (ir.ok)   setIngreso(ir.data.ingresoDiario)
    if (er.ok)   setEspera(er.data.tiempoPromedioEspera)
    if (afor.ok) setAforo(afor.data)
    if (alr.ok)  setTodas(alr.data)
    setLoading(false)
  }

  useEffect(() => { cargar() }, [])

  const fmtPeso = n => typeof n === 'number' ? `$${n.toLocaleString('es-CO')}` : '—'
  const fmtFecha = value => {
    if (!value) return '—'
    const fecha = new Date(value)
    if (Number.isNaN(fecha.getTime())) return String(value)
    return fecha.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' })
  }
  const badgeEstado = (estado) => {
    const cls = { ACTIVA:'badge-activa', EN_MANTENIMIENTO:'badge-mantenimiento', CERRADA:'badge-cerrada' }[estado] ?? 'badge-cerrada'
    return <span className={`badge ${cls}`}>{estado?.replace('_',' ')}</span>
  }

  const cierresClimaOrdenados = Array.isArray(cierresClima) ? [...cierresClima].reverse() : []

  if (loading) return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📊 ESTADÍSTICAS</h1>
      </div>
      <div style={{ color: 'var(--color-text-muted)', padding: '2rem', textAlign: 'center' }}>Cargando reportes…</div>
    </div>
  )

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1 className="page-title">📊 ESTADÍSTICAS Y REPORTES</h1>
          <p className="page-subtitle">Ingresos · Atracciones · Mantenimiento · Clima</p>
        </div>
        <button className="btn btn-ghost" onClick={cargar}>↺ Actualizar</button>
      </div>

      {/* KPIs principales */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(170px, 1fr))', gap: '0.75rem', marginBottom: '1.5rem' }}>
        <div className="stat-card">
          <span className="stat-label">💰 Ingreso diario</span>
          <span className="stat-value" style={{ fontSize: '1.3rem' }}>{fmtPeso(ingreso)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">⏱️ Espera promedio</span>
          <span className="stat-value">{espera != null ? `${espera.toFixed(1)} min` : '—'}</span>
        </div>
        {aforo && (
          <>
            <div className="stat-card">
              <span className="stat-label">👥 Aforo actual</span>
              <span className="stat-value" style={{ fontSize: '1.3rem' }}>{aforo.visitantesActuales}</span>
            </div>
            <div className="stat-card">
              <span className="stat-label">🏟️ Capacidad máx.</span>
              <span className="stat-value" style={{ fontSize: '1.3rem' }}>{aforo.capacidadMaxima}</span>
            </div>
          </>
        )}
        <div className="stat-card">
          <span className="stat-label">🔧 En mantenimiento</span>
          <span className="stat-value" style={{ color: 'var(--color-warning)' }}>{mantenimiento.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">⛈️ Eventos clima</span>
          <span className="stat-value" style={{ color: 'var(--color-danger)' }}>{cierresClima.length}</span>
        </div>
      </div>

      {/* Top atracciones */}
      <TablaEstadisticas
        titulo="🏆 TOP ATRACCIONES — MÁS VISITADAS"
        columnas={[
          { key: 'id',                    label: 'ID' },
          { key: 'nombre',                label: 'Atracción' },
          { key: 'tipo',                  label: 'Tipo', render: v => v?.replace('_',' ') },
          { key: 'visitantesAcumulados',  label: 'Visitantes' },
          { key: 'tiempoEsperaEstimado',  label: 'Espera (min)', render: v => v > 0 ? `${v} min` : '—' },
          { key: 'estado',                label: 'Estado', render: v => badgeEstado(v) },
        ]}
        filas={top}
        emptyMsg="Sin datos de atracciones. Carga el escenario de prueba desde Administración."
      />

      {/* Mantenimiento */}
      <TablaEstadisticas
        titulo="🔧 ALERTAS DE MANTENIMIENTO PREVENTIVO"
        columnas={[
          { key: 'id',                   label: 'ID' },
          { key: 'nombre',               label: 'Atracción' },
          { key: 'visitantesAcumulados', label: 'Visitantes acum.' },
          { key: 'motivoCierre',         label: 'Motivo', render: v => v ?? '—' },
          { key: 'zonaId',               label: 'Zona' },
        ]}
        filas={mantenimiento}
        emptyMsg="✅ Ninguna atracción en mantenimiento"
      />

      {/* Cierres por clima */}
      <TablaEstadisticas
        titulo="⛈️ CIERRES POR CONDICIONES CLIMÁTICAS"
        columnas={[
          { key: 'fechaCierre',    label: 'Fecha', render: v => fmtFecha(v) },
          { key: 'atraccionId',    label: 'ID' },
          { key: 'atraccionNombre', label: 'Atracción' },
          { key: 'tipoAtraccion',  label: 'Tipo', render: v => v?.replace('_',' ') },
          { key: 'tipoAlerta',     label: 'Alerta' },
          { key: 'motivoCierre',   label: 'Motivo' },
        ]}
        filas={cierresClimaOrdenados}
        emptyMsg="☀️ No hay cierres registrados por clima"
      />

      {/* Catálogo completo */}
      <TablaEstadisticas
        titulo="🎢 CATÁLOGO COMPLETO DE ATRACCIONES"
        columnas={[
          { key: 'id',                    label: 'ID' },
          { key: 'nombre',                label: 'Nombre' },
          { key: 'tipo',                  label: 'Tipo', render: v => v?.replace('_',' ') },
          { key: 'zonaId',                label: 'Zona' },
          { key: 'capacidadMaximaPorCiclo', label: 'Cap./ciclo' },
          { key: 'alturaMinima',          label: 'Alt. mín', render: v => v > 0 ? `${v} m` : '—' },
          { key: 'costoAdicional',        label: 'Costo adicional', render: v => v > 0 ? fmtPeso(v) : '—' },
          { key: 'visitantesAcumulados',  label: 'Visitantes' },
          { key: 'incidentesOperativos',  label: 'Incidentes' },
          { key: 'estado',                label: 'Estado', render: v => badgeEstado(v) },
        ]}
        filas={todas}
        emptyMsg="Sin atracciones cargadas"
      />
    </div>
  )
}
