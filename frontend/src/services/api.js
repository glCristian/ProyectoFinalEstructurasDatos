/**
 * services/api.js
 * Todas las llamadas HTTP al backend Javalin (http://localhost:8080)
 * Se usan a través del proxy Vite /api → localhost:8080
 * 
 * Auto-inyecta token de autenticación desde localStorage en todos los requests
 */

const BASE = '/api'
const STORAGE_KEY = 'techpark_sesion'

async function req(method, path, body) {
  // Leer token desde sessionStorage para mantener la sesión aislada por pestaña
  const sesion = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '{}')
  
  const opts = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(sesion.token ? { 'Authorization': `Bearer ${sesion.token}` } : {})
    },
  }
  if (body !== undefined) opts.body = JSON.stringify(body)
  const res = await fetch(BASE + path, opts)
  const text = await res.text()
  try { return { ok: res.ok, status: res.status, data: JSON.parse(text) } }
  catch { return { ok: res.ok, status: res.status, data: text } }
}



// ── Salud ─────────────────────────────────────────────────────────
export const ping = () => req('GET', '/ping')

// ── Atracciones ───────────────────────────────────────────────────
export const getAtracciones         = ()             => req('GET',    '/atracciones')
export const getAtraccion           = (id)           => req('GET',    `/atracciones/${id}`)
export const buscarAtraccionNombre  = (nombre)       => req('GET',    `/atracciones/buscar/${encodeURIComponent(nombre)}`)
export const crearAtraccion         = (body)         => req('POST',   '/atracciones', body)
export const actualizarAtraccion    = (id, body)     => req('PUT',    `/atracciones/${id}`, body)
export const actualizarEstado       = (id, estado, motivo = '') =>
  req('PUT', `/atracciones/${id}/estado`, { estado, motivo })
export const registrarRevision      = (id)           => req('PUT',    `/atracciones/${id}/revision`)
export const eliminarAtraccion      = (id)           => req('DELETE', `/atracciones/${id}`)
export const getTamanioCola         = (id)           => req('GET',    `/atracciones/${id}/cola/tamanio`)

// ── Zonas ─────────────────────────────────────────────────────────
export const getZonas   = ()     => req('GET',    '/zonas')
export const getZona    = (id)   => req('GET',    `/zonas/${id}`)
export const crearZona  = (body) => req('POST',   '/zonas', body)
export const actualizarZona = (id, body) => req('PUT', `/zonas/${id}`, body)
export const eliminarZona = (id) => req('DELETE', `/zonas/${id}`)

// ── Visitantes ────────────────────────────────────────────────────
export const getVisitantes     = ()         => req('GET',    '/visitantes')
export const getVisitante      = (id)       => req('GET',    `/visitantes/${id}`)
export const actualizarVisitante = (id, body) => req('PUT', `/visitantes/${id}`, body)
export const registrarVisitante = (body)    => req('POST',   '/visitantes', body)
export const retirarVisitante  = (id)       => req('DELETE', `/visitantes/${id}`)
export const getTicket         = (id)       => req('GET',    `/visitantes/${id}/ticket`)
export const comprarTicket     = (body)     => req('POST',   '/tickets/comprar', body)
export const postIngresarCola  = (visId, atracId) =>
  req('POST', `/visitantes/${visId}/cola/${atracId}`)
export const getPosicionCola   = (visId, atracId) =>
  req('GET', `/visitantes/${visId}/cola/${atracId}`)
export const getHistorial      = (id)       => req('GET',    `/visitantes/${id}/historial`)
export const getFavoritos      = (id)       => req('GET',    `/visitantes/${id}/favoritos`)
export const agregarFavorito   = (visId, atracId) =>
  req('POST', `/visitantes/${visId}/favoritos/${atracId}`)
export const eliminarFavorito  = (visId, atracId) =>
  req('DELETE', `/visitantes/${visId}/favoritos/${atracId}`)
export const recargarSaldo     = (id, monto) =>
  req('POST', `/visitantes/${id}/saldo/recargar`, { monto })
export const recargarSaldoVisitante = (id, monto) =>
  req('POST', `/visitantes/${id}/saldo/recargar`, { monto })
export const actualizarTicket  = (id, body) =>
  req('PUT', `/visitantes/${id}/ticket`, body)
export const cancelarTicket    = (id)       => req('DELETE', `/visitantes/${id}/ticket`)
export const getNotificaciones = (id)       => req('GET',    `/visitantes/${id}/notificaciones`)
export const clearNotificaciones = (id)     => req('DELETE', `/visitantes/${id}/notificaciones`)

// ── Operadores ────────────────────────────────────────────────────
export const getOperadores     = ()         => req('GET',    '/operadores')
export const getOperador       = (id)       => req('GET',    `/operadores/${id}`)
export const crearOperador     = (body)     => req('POST',   '/operadores', body)
export const asignarZona       = (id, zonaId) =>
  req('PUT', `/operadores/${id}/zona`, { zonaId })
export const eliminarOperador  = (id)       => req('DELETE', `/operadores/${id}`)
export const getRemocionAviso  = (id)       => req('GET', `/operadores/${id}/remocion-aviso`)
export const procesarSiguiente = (atracId)  =>
  req('POST', `/operadores/atracciones/${atracId}/procesar`)

// ── Clima ─────────────────────────────────────────────────────────
export const getClima           = ()       => req('GET',    '/clima')
export const postClima          = (tipo)   => req('POST',   '/clima/alerta', { tipo })
export const desactivarAlerta   = ()       => req('DELETE', '/clima/alerta')

// ── Rutas / Grafo ─────────────────────────────────────────────────
export const getRuta            = (origen, destino) =>
  req('GET', `/rutas/dijkstra?origen=${origen}&destino=${destino}`)
export const getBFS             = (origen) =>
  req('GET', `/rutas/bfs?origen=${origen}`)
export const getGrafo           = ()       => req('GET', '/rutas/grafo')
export const getClusters        = ()       => req('GET', '/rutas/clusters')
export const postConexion       = (idA, idB, peso) =>
  req('POST', '/rutas/conexion', { idA, idB, peso })
export const actualizarConexion = (idA, idB, peso) =>
  req('PUT', '/rutas/conexion', { idA, idB, peso })
export const eliminarConexion   = (idA, idB) =>
  req('DELETE', `/rutas/conexion?idA=${idA}&idB=${idB}`)
export const guardarPosicionNodo = (id, x, y) =>
  req('PUT', '/rutas/posicion', { id, x, y })
export const restaurarPosicionesMapa = () =>
  req('POST', '/rutas/posiciones/restaurar')

// ── Admin / Reportes ──────────────────────────────────────────────
export const getTopAtracciones  = (n = 5)  =>
  req('GET', `/admin/reportes/top-atracciones?n=${n}`)
export const getMantenimiento   = ()       => req('GET', '/admin/reportes/mantenimiento')
export const getCierresClima    = ()       => req('GET', '/admin/reportes/cierres-clima')
export const getIngresoDiario   = ()       => req('GET', '/admin/reportes/ingreso-diario')
export const getTiempoEspera    = ()       => req('GET', '/admin/reportes/tiempo-espera-promedio')
export const getAforo           = ()       => req('GET', '/admin/reportes/aforo')
export const cargarDatosPrueba  = ()       => req('POST', '/admin/datos/prueba')
export const cargarDesdeArchivo = (ruta)   => req('POST', '/admin/datos/archivo', { ruta })
export const actualizarCapacidadParque = (capacidad) =>
  req('PUT', '/admin/parque/capacidad', { capacidad })

// ── Autenticación ─────────────────────────────────────────────────
export const login    = (username, password) => req('POST', '/auth/login', { username, password })
export const logout   = ()                   => req('POST', '/auth/logout')
export const registro = (body)               => req('POST', '/auth/registro', body)
export const getMe    = ()                   => req('GET',  '/auth/me')
