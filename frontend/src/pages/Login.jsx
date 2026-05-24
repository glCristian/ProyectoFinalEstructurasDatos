import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { login as loginApi, registro as registroApi } from '../services/api'
import '../styles/Login.css'

export default function Login() {
  const [tab, setTab] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const [nombre, setNombre] = useState('')
  const [doc, setDoc] = useState('')
  const [edad, setEdad] = useState(18)
  const [altura, setAltura] = useState(1.6)

  const auth = useAuth()
  const navigate = useNavigate()

  async function handleLogin(e) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const res = await loginApi(username, password)
      if (!res.ok) {
        const msg = typeof res.data === 'string'
          ? res.data
          : res.data?.mensaje || res.data?.message || res.data?.error || 'Error en login'
        throw new Error(msg)
      }
      
      auth.login(res.data)
      const rol = (res.data.rol || '').toUpperCase()
      if (rol === 'ADMIN') navigate('/admin/dashboard')
      else if (rol === 'OPERADOR') navigate('/operador/dashboard')
      else navigate('/visitante/dashboard')
    } catch (err) {
      setError(err.message || 'Credenciales inválidas')
    } finally {
      setLoading(false)
    }
  }

  async function handleRegistro(e) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const body = {
        nombre,
        username,
        password,
        documento: doc,
        edad: Number(edad),
        altura: Number(altura)
      }
      const res = await registroApi(body)
      if (!res.ok) {
        const msg = typeof res.data === 'string'
          ? res.data
          : res.data?.mensaje || res.data?.message || res.data?.error || 'Error en registro'
        throw new Error(msg)
      }
      
      auth.login(res.data)
      navigate('/visitante/dashboard')
    } catch (err) {
      setError(err.message || 'Error al completar el registro')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">🎢 Tech-Park UQ</div>
          <p className="login-subtitle">Gestión y control de parques</p>
        </div>

        <div className="login-tabs">
          <button 
            type="button"
            onClick={() => { setTab('login'); setError(null); }} 
            className={`tab-btn ${tab === 'login' ? 'active' : ''}`}
          >
            Iniciar sesión
          </button>
          <button 
            type="button"
            onClick={() => { setTab('registro'); setError(null); }} 
            className={`tab-btn ${tab === 'registro' ? 'active' : ''}`}
          >
            Registrarse
          </button>
        </div>

        {error && <div className="login-error">{error}</div>}

        {tab === 'login' ? (
          <form onSubmit={handleLogin} className="login-form">
            <div className="form-group">
              <label>Usuario / Documento</label>
              <input 
                className="input"
                required
                value={username} 
                onChange={e => setUsername(e.target.value)} 
                placeholder="Ej. admin"
              />
            </div>
            
            <div className="form-group">
              <label>Contraseña</label>
              <input 
                className="input"
                required
                type="password" 
                value={password} 
                onChange={e => setPassword(e.target.value)} 
                placeholder="••••••••"
              />
            </div>
            
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Cargando...' : 'Entrar al Parque'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegistro} className="login-form registro-form">
            <div className="form-row">
              <div className="form-group">
                <label>Nombre y Apellido</label>
                <input required className="input" value={nombre} onChange={e => setNombre(e.target.value)} placeholder="Ej. Juan Pérez" />
              </div>
              <div className="form-group">
                <label>Documento</label>
                <input required className="input" value={doc} onChange={e => setDoc(e.target.value)} placeholder="Ej. 10203040" />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Nombre de Usuario</label>
                <input required className="input" value={username} onChange={e => setUsername(e.target.value)} placeholder="Ej. juanp" />
              </div>
              <div className="form-group">
                <label>Contraseña</label>
                <input required className="input" type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Edad</label>
                <input required min="1" max="120" className="input" type="number" value={edad} onChange={e => setEdad(e.target.value)} />
              </div>
              <div className="form-group">
                <label>Altura (m)</label>
                <input required min="0.5" max="2.5" className="input" type="number" step="0.01" value={altura} onChange={e => setAltura(e.target.value)} />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Cargando...' : 'Crear Cuenta'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
