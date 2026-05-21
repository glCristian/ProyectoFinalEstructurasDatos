package main.java.com.techparck.model;

//── Atributos, Constructor, Getters y Setters ────────────────────────────────────────────
public class Atraccion {
    private String id; 
    private String nombre; 
    private TipoAtraccion tipo; 
    private int capacidadMaxCiclo; 
    private double alturaMin; //en metros
    private int edadMin; 
    private double costoAdicional;
    private EstadoAtraccion estado; 
    private String motivoCierre; 
    private int visitantesAcumulados; //se bloquea cuando >= 500
    private double tiempoEspera; //en minutos
    private int incidentesOperativos;
    private String zonaId; 

    public Atraccion() {}

    public Atraccion(String id, String nombre, TipoAtraccion tipo,
                     int capacidadMaxCiclo, double alturaMin,
                     int edadMin, double costoAdicional, String zonaId) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidadMaxCiclo = capacidadMaxCiclo;
        this.alturaMin = alturaMin;
        this.edadMin = edadMin;
        this.costoAdicional = costoAdicional;
        this.zonaId = zonaId;
        this.estado = EstadoAtraccion.ACTIVA;
        this.visitantesAcumulados = 0;
        this.tiempoEspera = 0.0;
        this.incidentesOperativos = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoAtraccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoAtraccion tipo) {
        this.tipo = tipo;
    }

    public int getCapacidadMaxCiclo() {
        return capacidadMaxCiclo;
    }

    public void setCapacidadMaxCiclo(int capacidadMaxCiclo) {
        this.capacidadMaxCiclo = capacidadMaxCiclo;
    }

    public double getAlturaMin() {
        return alturaMin;
    }

    public void setAlturaMin(double alturaMin) {
        this.alturaMin = alturaMin;
    }

    public int getEdadMin() {
        return edadMin;
    }

    public void setEdadMin(int edadMin) {
        this.edadMin = edadMin;
    }

    public double getCostoAdicional() {
        return costoAdicional;
    }

    public void setCostoAdicional(double costoAdicional) {
        this.costoAdicional = costoAdicional;
    }

    public EstadoAtraccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAtraccion estado) {
        this.estado = estado;
    }

    public String getMotivoCierre() {
        return motivoCierre;
    }

    public void setMotivoCierre(String motivoCierre) {
        this.motivoCierre = motivoCierre;
    }

    public int getVisitantesAcumulados() {
        return visitantesAcumulados;
    }

    public void setVisitantesAcumulados(int visitantesAcumulados) {
        this.visitantesAcumulados = visitantesAcumulados;
    }

    public double getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(double tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public int getIncidentesOperativos() {
        return incidentesOperativos;
    }

    public void setIncidentesOperativos(int incidentesOperativos) {
        this.incidentesOperativos = incidentesOperativos;
    }

    public String getZonaId() {
        return zonaId;
    }

    public void setZonaId(String zonaId) {
        this.zonaId = zonaId;
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    public boolean registrarVisitante(){
        visitantesAcumulados++; 
        if (visitantesAcumulados>=500){
            estado = EstadoAtraccion.MANTENIMIENTO;
            motivoCierre = "En mantenimiento preventivo: limite de visitantes alcanzados(500)"; 
            return true;
        }
        return false;
    }

    public void registrarRevision(){
        this.visitantesAcumulados = 0;
        this.estado = EstadoAtraccion.ACTIVA;
        this.motivoCierre = null;
    }

    public boolean verificarRestriccionesVisitante(Visitante visitante){
        return visitante.getAltura() >= this.alturaMin && visitante.getEdad() >= this.edadMin;
    }

    public void registrarIncidente(){
        incidentesOperativos++;
    }

    @Override
    public String toString() {
        return "Atraccion{id='" + id + "', nombre='" + nombre + "', estado=" + estado + "}";
    }

    
}
