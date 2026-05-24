import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  getVisitante, actualizarVisitante, comprarTicket, recargarSaldoVisitante,
  getAtracciones, getTamanioCola, postIngresarCola, getPosicionCola, getHistorial, getFavoritos,
  agregarFavorito, eliminarFavorito, cancelarTicket
} from '../services/api.js'
import AtraccionCard from '../components/AtraccionCard.jsx'

const PRECIOS_FAMILIA = {
  2: 65000,
  3: 85000,
  4: 100000,
  5: 110000,
}

const PRECIO_GENERAL = 40000
const PRECIO_FASTPASS = 65000

const crearMiembrosVacios = (cantidad) => Array.from({ length: Math.max(0, cantidad) }, () => ({
  nombre: '',
  edad: '',
  altura: '',
}))

const normalizarMiembrosFamilia = (miembros, totalPersonas) => {
  const cantidad = Math.max(0, Number(totalPersonas) - 1)
  const base = crearMiembrosVacios(cantidad)
  return base.map((miembro, index) => {
    const fuente = miembros?.[index] || {}
    return {
      nombre: fuente.nombre || '',
      edad: fuente.edad ?? '',
      altura: fuente.altura ?? '',
    }
  })
}

export default function PanelVisitante() {
  const { sesion } = useAuth()
  const visitanteId = sesion?.userId

  const [tab, setTab] = useState('perfil')
  const [visitante, setVisitante] = useState(null)
  const [atracciones, setAtracciones] = useState([])
  const [historial, setHistorial] = useState([])
  const [favoritos, setFavoritos] = useState([])
  const [colasVirtuales, setColasVirtuales] = useState({})
  const [msg, setMsg] = useState('')
  const [loading, setLoading] = useState(true)

  const [perfilForm, setPerfilForm] = useState({ nombre: '', documento: '', edad: '', altura: '', username: '' })
  const [ticketForm, setTicketForm] = useState({ tipoTicket: 'GENERAL', numeroPersonas: 2, miembrosFamilia: crearMiembrosVacios(1) })
  const [recargaMonto, setRecargaMonto] = useState('')
  const [colaId, setColaId] = useState('')
  const [colaInfo, setColaInfo] = useState(null)
  const [favoritoSel, setFavoritoSel] = useState('')

  const tieneTicket = Boolean(visitante?.puedeDentrar && visitante?.ticket?.activo)

  const precioEstimado = () => {
    if (ticketForm.tipoTicket === 'FAST_PASS') return PRECIO_FASTPASS
    if (ticketForm.tipoTicket === 'GENERAL') return PRECIO_GENERAL
    return PRECIOS_FAMILIA[Number(ticketForm.numeroPersonas)] ?? 0
  }

  const cargar = async () => {
    if (!visitanteId) return
    setLoading(true)

    const [vr, ar, hr, fr] = await Promise.all([
      getVisitante(visitanteId),
      getAtracciones(),
      getHistorial(visitanteId),
      getFavoritos(visitanteId),
    ])

    if (vr.ok) {
      setVisitante(vr.data)
      setPerfilForm({
        nombre: vr.data.nombre || '',
        documento: vr.data.documento || '',
        edad: vr.data.edad ?? '',
        altura: vr.data.altura ?? '',
        username: vr.data.username || '',
      })

      if (vr.data.ticket) {
        const miembrosFamilia = (vr.data.ticket.miembrosDeLaFamilia || [])
          .filter(miembro => miembro?.id && miembro.id !== visitanteId)
          .map(miembro => ({
            nombre: miembro.nombre || '',
            edad: miembro.edad ?? '',
            altura: miembro.altura ?? '',
          }))
        setTicketForm({
          tipoTicket: vr.data.ticket.tipo || 'GENERAL',
          numeroPersonas: vr.data.ticket.numeroPersonasIncluidas || 1,
          miembrosFamilia: miembrosFamilia.length > 0
            ? miembrosFamilia
            : crearMiembrosVacios(Math.max(0, (vr.data.ticket.numeroPersonasIncluidas || 1) - 1)),
        })
      } else {
        setTicketForm(f => ({ ...f, miembrosFamilia: crearMiembrosVacios(Math.max(0, Number(f.numeroPersonas) - 1)) }))
      }
    }

    if (ar.ok) {
      setAtracciones(ar.data)
      const activas = Array.isArray(ar.data) ? ar.data.filter(a => a.estado === 'ACTIVA') : []
      const colas = await Promise.all(activas.map(async a => {
        const cr = await getTamanioCola(a.id)
        return [a.id, cr.ok ? (cr.data?.tamanioCola ?? 0) : 0]
      }))
      setColasVirtuales(Object.fromEntries(colas))
    }
    if (hr.ok) setHistorial(Array.isArray(hr.data) ? hr.data : [])
    if (fr.ok) setFavoritos(Array.isArray(fr.data) ? fr.data : [])
    setLoading(false)
  }

  useEffect(() => {
    cargar()
  }, [visitanteId])

  useEffect(() => {
    if (!loading && !tieneTicket && tab === 'atracciones') {
      setTab('comprar')
    }
  }, [loading, tieneTicket, tab])

  const flash = (ok, okMsg, errMsg) => {
    setMsg(ok ? `✅ ${okMsg}` : `❌ ${errMsg}`)
  }

  const handleActualizarPerfil = async () => {
    if (!visitanteId) return
    if (!perfilForm.nombre.trim() || !perfilForm.documento.trim() || !perfilForm.username.trim()) {
      flash(false, '', 'Completa usuario, nombre e identificación')
      return
    }

    const edad = Number(perfilForm.edad)
    const altura = Number(perfilForm.altura)
    if (!Number.isFinite(edad) || edad < 0 || !Number.isFinite(altura) || altura <= 0) {
      flash(false, '', 'Edad y estatura deben ser valores válidos')
      return
    }

    const r = await actualizarVisitante(visitanteId, {
      nombre: perfilForm.nombre.trim(),
      documento: perfilForm.documento.trim(),
      edad,
      altura,
      username: perfilForm.username.trim(),
    })

    flash(r.ok, 'Perfil actualizado', r.data || 'Error al actualizar')
    if (r.ok) cargar()
  }

  const handleComprarTicket = async () => {
    if (!visitanteId) return

    const body = {
      visitanteId,
      tipoTicket: ticketForm.tipoTicket,
    }

    try {
      if (ticketForm.tipoTicket === 'FAMILIAR') {
        const numeroPersonas = Number(ticketForm.numeroPersonas)
        const miembrosFamilia = ticketForm.miembrosFamilia.map((miembro, index) => {
          const nombre = (miembro.nombre || '').trim()
          const edad = Number(miembro.edad)
          const altura = Number(miembro.altura)
          if (!nombre || !Number.isFinite(edad) || edad < 0 || !Number.isFinite(altura) || altura <= 0) {
            throw new Error(`Completa nombre, edad y altura del integrante ${index + 1}`)
          }
          return { nombre, edad, altura }
        })

        if (miembrosFamilia.length !== numeroPersonas - 1) {
          throw new Error('La cantidad de integrantes adicionales no coincide con el número de personas')
        }

        body.numeroPersonas = numeroPersonas
        body.miembrosFamilia = miembrosFamilia
      }
    } catch (error) {
      flash(false, '', error.message || 'Datos de familia inválidos')
      return
    }

    const r = await comprarTicket(body)
    if (!r.ok) {
      flash(false, '', r.data || 'No se pudo comprar la entrada')
      return
    }

    const precioFinal = r.data?.precioFinal ?? precioEstimado()
    flash(true, `Entrada comprada por $${Number(precioFinal).toLocaleString()}`, '')
    setTab('perfil')
    cargar()
  }

  const handleRecargarSaldo = async () => {
    if (!visitanteId || !recargaMonto) return

    const monto = Number(recargaMonto)
    if (!Number.isFinite(monto) || monto <= 0) {
      flash(false, '', 'Monto inválido')
      return
    }

    const r = await recargarSaldoVisitante(visitanteId, monto)
    flash(r.ok, `Saldo actualizado: $${Number(r.data?.saldoNuevo ?? 0).toLocaleString()}`, 'Error al recargar')
    if (r.ok) {
      setRecargaMonto('')
      cargar()
    }
  }

  const handleUnirseCola = async (atraccionId) => {
    if (!visitanteId) return
    if (!tieneTicket) {
      flash(false, '', 'Debes comprar una entrada primero')
      return
    }

    const r = await postIngresarCola(visitanteId, atraccionId)
    const detalle = typeof r.data === 'string' ? r.data : r.data?.mensaje

    if (!r.ok) {
      flash(false, '', detalle || 'No se pudo ingresar a la cola')
      return
    }

    setColaId(atraccionId)
    const ingresoTitular = Array.isArray(r.data?.ingresados)
      ? r.data.ingresados.some(miembro => miembro?.id === visitanteId)
      : true

    if (ingresoTitular) {
      const pr = await getPosicionCola(visitanteId, atraccionId)
      if (pr.ok) setColaInfo(pr.data)
    } else {
      setColaInfo(null)
    }

    flash(true, detalle || 'Ingresado a la cola', '')
  }

  const refrescarCola = async () => {
    if (!visitanteId || !colaId) return
    const pr = await getPosicionCola(visitanteId, colaId)
    if (pr.ok) setColaInfo(pr.data)
  }

  const handleAgregarFavorito = async () => {
    if (!visitanteId || !favoritoSel) return
    const r = await agregarFavorito(visitanteId, favoritoSel)
    flash(r.ok, 'Favorito agregado', r.data || 'No se pudo agregar')
    if (r.ok) {
      setFavoritoSel('')
      cargar()
    }
  }

  const handleQuitarFavorito = async (atraccionId) => {
    if (!visitanteId) return
    const r = await eliminarFavorito(visitanteId, atraccionId)
    flash(r.ok, 'Favorito eliminado', r.data || 'No se pudo eliminar')
    if (r.ok) cargar()
  }

  const handleCancelarTicket = async () => {
    if (!visitanteId) return
    const ok = window.confirm('¿Cancelar la entrada actual?')
    if (!ok) return
    const r = await cancelarTicket(visitanteId)
    flash(r.ok, 'Entrada cancelada', r.data || 'No se pudo cancelar')
    if (r.ok) cargar()
  }

  const nombreDe = (id) => atracciones.find(a => a.id === id)?.nombre ?? id

  const renderPerfil = () => (
    <div style={{ display: 'grid', gap: '1rem' }}>
      <div className="card" style={{ display: 'grid', gap: '1rem' }}>
        <div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
            SALDO VIRTUAL
          </div>
          <div style={{ fontSize: '2rem', fontFamily: 'var(--font-display)', color: 'var(--color-primary)' }}>
            ${Number(visitante.saldoVirtual || 0).toLocaleString()}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: '0.5rem 1rem', fontSize: '0.9rem' }}>
          <div><span style={{ color: 'var(--color-text-muted)' }}>Nombre: </span><strong>{visitante.nombre}</strong></div>
          <div><span style={{ color: 'var(--color-text-muted)' }}>Usuario: </span><strong>{visitante.username || '—'}</strong></div>
          <div><span style={{ color: 'var(--color-text-muted)' }}>Documento: </span><strong>{visitante.documento}</strong></div>
          <div><span style={{ color: 'var(--color-text-muted)' }}>Edad: </span><strong>{visitante.edad}</strong></div>
          <div><span style={{ color: 'var(--color-text-muted)' }}>Altura: </span><strong>{visitante.altura} m</strong></div>
          <div><span style={{ color: 'var(--color-text-muted)' }}>Puede entrar: </span><strong>{tieneTicket ? 'Sí' : 'No'}</strong></div>
        </div>

        <div style={{ borderTop: '1px solid var(--color-surface2)', paddingTop: '1rem' }}>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.5rem', letterSpacing: '0.04em' }}>
            TICKET ACTUAL
          </div>
          {visitante.ticket ? (
            <div style={{ display: 'grid', gap: '0.4rem', fontSize: '0.9rem' }}>
              <div><strong>{visitante.ticket.tipo}</strong> · ${Number(visitante.ticket.precioCompra || visitante.ticket.precio || 0).toLocaleString()}</div>
              <div>Personas incluidas: <strong>{visitante.ticket.numeroPersonasIncluidas || 1}</strong></div>
              <div>Fecha compra: <strong>{visitante.ticket.fechaCompra ? new Date(visitante.ticket.fechaCompra).toLocaleString() : '—'}</strong></div>
              {visitante.ticket.tipo === 'FAMILIAR' && Array.isArray(visitante.ticket.miembrosDeLaFamilia) && (
                <div>
                  Grupo:
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginTop: '0.35rem' }}>
                    {visitante.ticket.miembrosDeLaFamilia.map(miembro => (
                      <span key={miembro.id} style={{ background: 'var(--color-surface2)', padding: '2px 8px', borderRadius: '999px', fontSize: '0.75rem' }}>
                        {miembro.nombre}{miembro.edad != null ? ` · ${miembro.edad} años` : ''}{miembro.altura != null ? ` · ${miembro.altura} m` : ''}
                      </span>
                    ))}
                  </div>
                </div>
              )}
              <button className="btn btn-ghost" style={{ justifySelf: 'start' }} onClick={handleCancelarTicket}>
                Cancelar entrada
              </button>
            </div>
          ) : (
            <div style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
              No tienes una entrada comprada todavía.
            </div>
          )}
        </div>
      </div>

      <div className="card" style={{ display: 'grid', gap: '1rem' }}>
        <div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
            DATOS PERSONALES
          </div>
          <div style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Puedes actualizar tu usuario, nombre, identificación, edad y estatura desde aquí.
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: '0.75rem' }}>
          <div className="form-group">
            <label>Usuario</label>
            <input className="input" value={perfilForm.username} onChange={e => setPerfilForm(f => ({ ...f, username: e.target.value }))} placeholder="Usuario" />
          </div>
          <div className="form-group">
            <label>Nombre</label>
            <input className="input" value={perfilForm.nombre} onChange={e => setPerfilForm(f => ({ ...f, nombre: e.target.value }))} placeholder="Nombre" />
          </div>
          <div className="form-group">
            <label>Identificación</label>
            <input className="input" value={perfilForm.documento} onChange={e => setPerfilForm(f => ({ ...f, documento: e.target.value }))} placeholder="Documento" />
          </div>
          <div className="form-group">
            <label>Edad</label>
            <input className="input" type="number" min="0" value={perfilForm.edad} onChange={e => setPerfilForm(f => ({ ...f, edad: e.target.value }))} placeholder="Edad" />
          </div>
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label>Estatura</label>
            <input className="input" type="number" step="0.01" min="0" value={perfilForm.altura} onChange={e => setPerfilForm(f => ({ ...f, altura: e.target.value }))} placeholder="Estatura" />
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={handleActualizarPerfil}>Guardar cambios</button>
        </div>
      </div>
    </div>
  )

  const renderComprar = () => (
    <div className="card" style={{ display: 'grid', gap: '1rem' }}>
      <div>
        <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
          COMPRAR ENTRADA
        </div>
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem', marginBottom: '1rem' }}>
          Primero recarga saldo, luego compra tu entrada. El saldo se descuenta al comprar.
        </div>
      </div>

      <div style={{ display: 'grid', gap: '0.75rem', maxWidth: 600 }}>
        <div className="form-group">
          <label>Tipo de ticket</label>
          <select className="select" value={ticketForm.tipoTicket} onChange={e => setTicketForm(f => ({
            ...f,
            tipoTicket: e.target.value,
            miembrosFamilia: e.target.value === 'FAMILIAR' && f.miembrosFamilia.length === 0
              ? crearMiembrosVacios(Math.max(1, Number(f.numeroPersonas) - 1))
              : f.miembrosFamilia,
          }))}>
            <option value="GENERAL">General</option>
            <option value="FAMILIAR">Familiar</option>
            <option value="FAST_PASS">Fast Pass</option>
          </select>
        </div>

        {ticketForm.tipoTicket === 'FAMILIAR' && (
          <>
            <div className="form-group">
              <label>Número de personas</label>
              <select className="select" value={ticketForm.numeroPersonas} onChange={e => {
                const numeroPersonas = Number(e.target.value)
                setTicketForm(f => ({
                  ...f,
                  numeroPersonas,
                  miembrosFamilia: normalizarMiembrosFamilia(f.miembrosFamilia, numeroPersonas),
                }))
              }}>
                {[2, 3, 4, 5].map(n => <option key={n} value={n}>{n} personas</option>)}
              </select>
            </div>
            <div style={{ display: 'grid', gap: '0.75rem' }}>
              {ticketForm.miembrosFamilia.map((miembro, index) => (
                <div key={index} style={{ background: 'var(--color-surface2)', borderRadius: 'var(--radius)', padding: '0.9rem', display: 'grid', gap: '0.75rem' }}>
                  <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', letterSpacing: '0.04em' }}>
                    Integrante adicional {index + 1}
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: '0.75rem' }}>
                    <div className="form-group">
                      <label>Nombre</label>
                      <input
                        className="input"
                        value={miembro.nombre}
                        onChange={e => setTicketForm(f => {
                          const miembrosFamilia = [...f.miembrosFamilia]
                          miembrosFamilia[index] = { ...miembrosFamilia[index], nombre: e.target.value }
                          return { ...f, miembrosFamilia }
                        })}
                        placeholder="Nombre"
                      />
                    </div>
                    <div className="form-group">
                      <label>Edad</label>
                      <input
                        className="input"
                        type="number"
                        min="0"
                        value={miembro.edad}
                        onChange={e => setTicketForm(f => {
                          const miembrosFamilia = [...f.miembrosFamilia]
                          miembrosFamilia[index] = { ...miembrosFamilia[index], edad: e.target.value }
                          return { ...f, miembrosFamilia }
                        })}
                        placeholder="Edad"
                      />
                    </div>
                    <div className="form-group">
                      <label>Altura</label>
                      <input
                        className="input"
                        type="number"
                        min="0"
                        step="0.01"
                        value={miembro.altura}
                        onChange={e => setTicketForm(f => {
                          const miembrosFamilia = [...f.miembrosFamilia]
                          miembrosFamilia[index] = { ...miembrosFamilia[index], altura: e.target.value }
                          return { ...f, miembrosFamilia }
                        })}
                        placeholder="Altura"
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        <div style={{ background: 'var(--color-surface2)', borderRadius: 'var(--radius)', padding: '0.9rem', fontSize: '0.95rem' }}>
          Precio estimado: <strong>${precioEstimado().toLocaleString()}</strong>
          {ticketForm.tipoTicket === 'FAMILIAR' && (
            <div style={{ color: 'var(--color-text-muted)', marginTop: '0.35rem', fontSize: '0.85rem' }}>
              El sistema crea miembros internos asociados al ticket y valida a cada uno al entrar a una atracción.
            </div>
          )}
          {ticketForm.tipoTicket === 'FAST_PASS' && (
            <div style={{ color: 'var(--color-text-muted)', marginTop: '0.35rem', fontSize: '0.85rem' }}>
              Acceso gratuito a los costos adicionales de atracciones.
            </div>
          )}
        </div>

        <button className="btn btn-primary" onClick={handleComprarTicket}>Comprar</button>
      </div>
    </div>
  )

  const renderRecarga = () => (
    <div className="card" style={{ display: 'grid', gap: '1rem', maxWidth: 520 }}>
      <div>
        <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
          RECARGAR SALDO
        </div>
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
          El saldo virtual se usa para comprar entradas y pagar extras de algunas atracciones.
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <input className="input" value={recargaMonto} onChange={e => setRecargaMonto(e.target.value)} placeholder="Monto a recargar" />
        <button className="btn btn-ghost" onClick={handleRecargarSaldo}>Recargar</button>
      </div>
    </div>
  )

  const renderAtracciones = () => (
    <div style={{ display: 'grid', gap: '1rem' }}>
      {!tieneTicket ? (
        <div className="card" style={{ color: 'var(--color-text-muted)', padding: '1.2rem' }}>
          Debes comprar una entrada primero.
        </div>
      ) : (
        <>
          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              FILA VIRTUAL
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <select className="select" style={{ flex: 1, minWidth: 180 }} value={colaId} onChange={e => setColaId(e.target.value)}>
                <option value="">— atracción —</option>
                {atracciones.filter(a => a.estado === 'ACTIVA').map(a => (
                  <option key={a.id} value={a.id}>{a.nombre}</option>
                ))}
              </select>
              <button className="btn btn-primary" onClick={() => handleUnirseCola(colaId)} disabled={!colaId}>Unirse</button>
              <button className="btn btn-ghost" onClick={refrescarCola} disabled={!colaId}>Actualizar</button>
            </div>
            {colaInfo && colaInfo.atraccionId === colaId && (
              <div style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}>
                <div>Posición: <strong>{colaInfo.posicion > 0 ? colaInfo.posicion : '—'}</strong> / {colaInfo.tamanio}</div>
                <div>Espera estimada: <strong>{colaInfo.esperaMin?.toFixed(1)} min</strong></div>
              </div>
            )}
          </div>

          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              FAVORITOS
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.6rem' }}>
              <select className="select" style={{ flex: 1 }} value={favoritoSel} onChange={e => setFavoritoSel(e.target.value)}>
                <option value="">— agregar atracción —</option>
                {atracciones.filter(a => !favoritos.includes(a.id)).map(a => (
                  <option key={a.id} value={a.id}>{a.nombre}</option>
                ))}
              </select>
              <button className="btn btn-secondary" onClick={handleAgregarFavorito} disabled={!favoritoSel}>Agregar</button>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
              {favoritos.length === 0 ? (
                <span style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>Sin favoritos</span>
              ) : favoritos.map(id => (
                <span key={id} style={{ background: 'rgba(0,229,255,0.1)', color: 'var(--color-primary)', padding: '2px 8px', borderRadius: '999px', fontSize: '0.75rem', display: 'flex', gap: '0.35rem', alignItems: 'center' }}>
                  ⭐ {nombreDe(id)}
                  <button className="btn btn-danger" style={{ padding: '0 6px', fontSize: '0.7rem' }} onClick={() => handleQuitarFavorito(id)}>✕</button>
                </span>
              ))}
            </div>
          </div>

          <div className="card">
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              HISTORIAL
            </div>
            {historial.length === 0 ? (
              <div style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>Sin visitas registradas</div>
            ) : (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                {historial.map((id, i) => (
                  <span key={`${id}-${i}`} style={{ background: 'var(--color-surface2)', padding: '2px 8px', borderRadius: '999px', fontSize: '0.75rem' }}>
                    {nombreDe(id)}
                  </span>
                ))}
              </div>
            )}
          </div>

          <div style={{ marginTop: '0.5rem' }}>
            <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--color-primary)', marginBottom: '0.75rem', letterSpacing: '0.04em' }}>
              ATRACCIONES Y TIEMPOS DE ESPERA
            </div>
            <div className="card-grid">
              {atracciones.map(a => (
                <AtraccionCard
                  key={a.id}
                  atraccion={{ ...a, colaVirtual: colasVirtuales[a.id] ?? 0 }}
                  onAccion={a.estado === 'ACTIVA' ? () => handleUnirseCola(a.id) : null}
                  labelAccion="Unirse a fila"
                />
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )

  const tabs = [
    { id: 'perfil', label: 'Mi Perfil' },
    { id: 'comprar', label: 'Comprar Entrada' },
    { id: 'recarga', label: 'Recargar Saldo' },
  ]

  if (tieneTicket) {
    tabs.push({ id: 'atracciones', label: 'Mis Atracciones' })
  }

  if (loading) {
    return (
      <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
        Cargando tu panel…
      </div>
    )
  }

  if (!visitante) {
    return (
      <div className="card" style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '2rem' }}>
        No se pudo cargar tu información de visitante.
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">👤 MI PANEL</h1>
        <p className="page-subtitle">Registro, compra de entrada, saldo y atracciones</p>
      </div>

      {msg && (
        <div style={{ marginBottom: '1rem', fontSize: '0.85rem', color: msg.startsWith('❌') ? 'var(--color-danger)' : 'var(--color-success)' }}>
          {msg}
        </div>
      )}

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1rem' }}>
        {tabs.map(item => (
          <button
            key={item.id}
            className="btn"
            onClick={() => setTab(item.id)}
            style={{
              background: tab === item.id ? 'var(--color-primary)' : 'var(--color-surface2)',
              color: tab === item.id ? '#00131a' : 'var(--color-text)',
              border: 'none',
            }}
          >
            {item.label}
          </button>
        ))}
      </div>

      {tab === 'perfil' && renderPerfil()}
      {tab === 'comprar' && renderComprar()}
      {tab === 'recarga' && renderRecarga()}
      {tab === 'atracciones' && renderAtracciones()}
    </div>
  )
}
