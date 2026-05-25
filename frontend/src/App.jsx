import React from 'react'
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'

// Páginas
import Login from './pages/Login.jsx'
import PanelVisitante from './pages/PanelVisitante.jsx'
import PanelOperador from './pages/PanelOperador.jsx'
import PanelOperadoresAdmin from './pages/PanelOperadoresAdmin.jsx'
import PanelVisitantesAdmin from './pages/PanelVisitantesAdmin.jsx'
import PanelAdmin from './pages/PanelAdmin.jsx'
import PanelRutas from './pages/PanelRutas.jsx'
import Estadisticas from './pages/Estadisticas.jsx'
import MapaInteractivo from './pages/MapaInteractivo.jsx'

// Componentes
import ProtectedRoute from './components/ProtectedRoute.jsx'
import LayoutAdmin from './components/LayoutAdmin.jsx'
import LayoutOperador from './components/LayoutOperador.jsx'
import LayoutVisitante from './components/LayoutVisitante.jsx'

/**
 * Router layout del sistema Tech-Park UQ
 * 
 * Estructura:
 * ├─ /login                    → Login (sin sidebar)
 * ├─ /admin/*                  → LayoutAdmin + rutas protegidas [ADMIN]
 * ├─ /operador/*               → LayoutOperador + rutas protegidas [OPERADOR]
 * ├─ /visitante/*              → LayoutVisitante + rutas protegidas [VISITANTE]
 * └─ /                          → Inicio (público)
 */

// Componente envoltorio para mantener MapaInteractivo siempre montado
function AppContent() {
  const location = useLocation()
  const isMapRoute = location.pathname === '/visitante/mapa'

  return (
    <>
      <Routes>
        {/* ┌────────────────────────────────────────────────────────┐
            │ RUTA PÚBLICA: LOGIN (sin sidebar)                     │
            └────────────────────────────────────────────────────────┘ */}
        <Route path="/login" element={<Login />} />

        {/* ┌────────────────────────────────────────────────────────┐
          │ RUTA PÚBLICA: RAÍZ → LOGIN                            │
          └────────────────────────────────────────────────────────┘ */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* ┌────────────────────────────────────────────────────────┐
            │ RUTAS PROTEGIDAS: ADMIN                               │
            │ Requisito: rol = 'ADMIN'                              │
            └────────────────────────────────────────────────────────┘ */}
        <Route
          path="/admin/*"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <LayoutAdmin />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<PanelAdmin />} />
          <Route path="atracciones" element={<PanelAdmin />} />
          <Route path="zonas" element={<PanelAdmin />} />
          <Route path="operadores" element={<PanelOperadoresAdmin />} />
          <Route path="visitantes" element={<PanelVisitantesAdmin />} />
          <Route path="rutas" element={<PanelRutas />} />
          <Route path="mapa" element={<MapaInteractivo />} />
          <Route path="estadisticas" element={<Estadisticas />} />
        </Route>

        {/* ┌────────────────────────────────────────────────────────┐
            │ RUTAS PROTEGIDAS: OPERADOR                            │
            │ Requisito: rol = 'OPERADOR'                           │
            └────────────────────────────────────────────────────────┘ */}
        <Route
          path="/operador/*"
          element={
            <ProtectedRoute roles={['OPERADOR']}>
              <LayoutOperador />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<PanelOperador />} />
          <Route path="atracciones" element={<PanelOperador />} />
          <Route path="zona-asignada" element={<PanelOperador />} />
        </Route>

        {/* ┌────────────────────────────────────────────────────────┐
            │ RUTAS PROTEGIDAS: VISITANTE                           │
            │ Requisito: rol = 'VISITANTE'                          │
            └────────────────────────────────────────────────────────┘ */}
        <Route
          path="/visitante/*"
          element={
            <ProtectedRoute roles={['VISITANTE']}>
              <LayoutVisitante />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<PanelVisitante />} />
          <Route path="atracciones" element={<PanelVisitante />} />
          <Route path="rutas" element={<Navigate to="/visitante/dashboard" replace />} />
          <Route path="mapa" element={<MapaInteractivo />} />
          <Route path="ticket" element={<PanelVisitante />} />
        </Route>

        {/* ┌────────────────────────────────────────────────────────┐
            │ RUTA CATCH-ALL: cualquier otra ruta → /login          │
            └────────────────────────────────────────────────────────┘ */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>

      {/* Mapa siempre montado pero oculto cuando no está en /visitante/mapa
          Esto preserva el estado del canvas y las posiciones guardadas */}
      <div style={{ display: isMapRoute ? 'block' : 'none', position: 'absolute' }} />
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  )
}
