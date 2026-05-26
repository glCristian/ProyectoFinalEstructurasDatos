package com.techpark.model;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa una atracción del parque Tech-Park UQ.
 * Es un nodo dentro del grafo del parque.
 */
public class Atraccion {

    // ── Identificación ─────────────────────────────────────────────────
    private String id;
    private String nombre;
    private TipoAtraccion tipo;

    // ── Capacidad y seguridad ──────────────────────────────────────────
    private int capacidadMaximaPorCiclo;
    private double alturaMinima;       // en metros
    private int    edadMinima;         // en años
    private double costoAdicional;     // costo extra para tickets General/Familiar

    // ── Estado y operación ────────────────────────────────────────────
    private EstadoAtraccion estado;
    private String motivoCierre;       // se registra cuando estado != ACTIVA

    // ── Métricas ───────────────────────────────────────────────────────
    private int visitantesAcumulados;  // se bloquea a >= 500
    private double tiempoEsperaEstimado; // en minutos
    private int incidentesOperativos;

    // ── Zona a la que pertenece ────────────────────────────────────────
    private String zonaId;

    // ── Mapa interactivo ───────────────────────────────────────────────
    private Double posicionX;
    private Double posicionY;

    // ── Conexiones del grafo ──────────────────────────────────────────
    private List<String> conexiones;
    private Map<String, Double> pesosConexiones;

    // ── Constructores ──────────────────────────────────────────────────

    public Atraccion() {}

    public Atraccion(String id, String nombre, TipoAtraccion tipo,
                     int capacidadMaximaPorCiclo, double alturaMinima,
                     int edadMinima, double costoAdicional, String zonaId) {
        this.id                    = id;
        this.nombre                = nombre;
        this.tipo                  = tipo;
        this.capacidadMaximaPorCiclo = capacidadMaximaPorCiclo;
        this.alturaMinima          = alturaMinima;
        this.edadMinima            = edadMinima;
        this.costoAdicional        = costoAdicional;
        this.zonaId                = zonaId;
        this.estado                = EstadoAtraccion.ACTIVA;
        this.visitantesAcumulados  = 0;
        this.tiempoEsperaEstimado  = 0.0;
        this.incidentesOperativos  = 0;
        this.conexiones            = new ArrayList<>();
        this.pesosConexiones       = new LinkedHashMap<>();
    }

    /** Agrega una conexión si aún no existe. */
    public boolean agregarConexion(String idAtraccion) {
        if (idAtraccion == null || idAtraccion.isBlank()) return false;
        if (conexiones == null) conexiones = new ArrayList<>();
        if (conexiones.contains(idAtraccion)) return false;
        conexiones.add(idAtraccion);
        return true;
    }

    /** Registra o actualiza el peso de una conexión. */
    public void registrarPesoConexion(String idAtraccion, double peso) {
        if (pesosConexiones == null) pesosConexiones = new LinkedHashMap<>();
        pesosConexiones.put(idAtraccion, peso);
    }

    /** Elimina una conexión existente. */
    public boolean quitarConexion(String idAtraccion) {
        if (conexiones == null || idAtraccion == null) return false;
        if (pesosConexiones != null) {
            pesosConexiones.remove(idAtraccion);
        }
        return conexiones.remove(idAtraccion);
    }

    /** Limpia todas las conexiones registradas. */
    public void limpiarConexiones() {
        if (conexiones == null) conexiones = new ArrayList<>();
        conexiones.clear();
        if (pesosConexiones == null) pesosConexiones = new LinkedHashMap<>();
        pesosConexiones.clear();
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    /** Incrementa el contador de visitantes y aplica mantenimiento si >= 500. */
    public boolean registrarVisitante() {
        visitantesAcumulados++;
        if (visitantesAcumulados >= 500) {
            estado = EstadoAtraccion.EN_MANTENIMIENTO;
            motivoCierre = "Mantenimiento preventivo: límite de 500 visitantes alcanzado";
            return true; // indica que se activó mantenimiento
        }
        return false;
    }

    /** Registra una revisión técnica y reactiva la atracción. */
    public void registrarRevisionTecnica() {
        this.visitantesAcumulados = 0;
        this.estado = EstadoAtraccion.ACTIVA;
        this.motivoCierre = null;
    }

    /** Cierra la atracción con un motivo específico. */
    public void cerrar(String motivo) {
        this.estado = EstadoAtraccion.CERRADA;
        this.motivoCierre = motivo;
    }

    /** Verifica si un visitante cumple las restricciones de seguridad. */
    public boolean cumpleRestricciones(Visitante visitante) {
        return visitante.getAltura() >= this.alturaMinima
            && visitante.getEdad() >= this.edadMinima;
    }

    /** Incrementa el contador de incidentes operativos. */
    public void registrarIncidente() {
        incidentesOperativos++;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoAtraccion getTipo() { return tipo; }
    public void setTipo(TipoAtraccion tipo) { this.tipo = tipo; }

    public int getCapacidadMaximaPorCiclo() { return capacidadMaximaPorCiclo; }
    public void setCapacidadMaximaPorCiclo(int capacidadMaximaPorCiclo) { this.capacidadMaximaPorCiclo = capacidadMaximaPorCiclo; }

    public double getAlturaMinima() { return alturaMinima; }
    public void setAlturaMinima(double alturaMinima) { this.alturaMinima = alturaMinima; }

    public int getEdadMinima() { return edadMinima; }
    public void setEdadMinima(int edadMinima) { this.edadMinima = edadMinima; }

    public double getCostoAdicional() { return costoAdicional; }
    public void setCostoAdicional(double costoAdicional) { this.costoAdicional = costoAdicional; }

    public EstadoAtraccion getEstado() { return estado; }
    public void setEstado(EstadoAtraccion estado) { this.estado = estado; }

    public String getMotivoCierre() { return motivoCierre; }
    public void setMotivoCierre(String motivoCierre) { this.motivoCierre = motivoCierre; }

    public int getVisitantesAcumulados() { return visitantesAcumulados; }
    public void setVisitantesAcumulados(int visitantesAcumulados) { this.visitantesAcumulados = visitantesAcumulados; }

    public double getTiempoEsperaEstimado() { return tiempoEsperaEstimado; }
    public void setTiempoEsperaEstimado(double tiempoEsperaEstimado) { this.tiempoEsperaEstimado = tiempoEsperaEstimado; }

    public int getIncidentesOperativos() { return incidentesOperativos; }
    public void setIncidentesOperativos(int incidentesOperativos) { this.incidentesOperativos = incidentesOperativos; }

    public String getZonaId() { return zonaId; }
    public void setZonaId(String zonaId) { this.zonaId = zonaId; }

    public Double getPosicionX() { return posicionX; }
    public void setPosicionX(Double posicionX) { this.posicionX = posicionX; }

    public Double getPosicionY() { return posicionY; }
    public void setPosicionY(Double posicionY) { this.posicionY = posicionY; }

    public List<String> getConexiones() {
        if (conexiones == null) conexiones = new ArrayList<>();
        return conexiones;
    }

    public void setConexiones(List<String> conexiones) {
        this.conexiones = conexiones != null ? new ArrayList<>(conexiones) : new ArrayList<>();
    }

    public Map<String, Double> getPesosConexiones() {
        if (pesosConexiones == null) pesosConexiones = new LinkedHashMap<>();
        return pesosConexiones;
    }

    public void setPesosConexiones(Map<String, Double> pesosConexiones) {
        this.pesosConexiones = pesosConexiones != null ? new LinkedHashMap<>(pesosConexiones) : new LinkedHashMap<>();
    }

    public double getPesoConexion(String idAtraccion) {
        if (pesosConexiones == null) return Double.NaN;
        return pesosConexiones.getOrDefault(idAtraccion, Double.NaN);
    }

    public boolean tienePosicion() {
        return posicionX != null && posicionY != null;
    }

    @Override
    public String toString() {
        return "Atraccion{id='" + id + "', nombre='" + nombre + "', estado=" + estado + "}";
    }
}
