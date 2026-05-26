package com.techpark.model;

/**
 * Representa un operador del parque Tech-Park UQ.
 * Solo puede gestionar las atracciones de la zona que le fue asignada.
 */
public class Operador {

    private String id;
    private String nombre;
    private String documento;
    private String zonaAsignadaId; // zona a la que fue asignado (solo una)
    private boolean activo;
    private String username;
    private String passwordHash;

    // ── Constructores ──────────────────────────────────────────────────

    public Operador() {
    }

    public Operador(String id, String nombre, String documento, String username, String passwordHash) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.username = username;
        this.passwordHash = passwordHash;
        this.activo = true;
    }

    public Operador(String id, String nombre, String documento) {
        this(id, nombre, documento, null, null);
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    /** Asigna al operador a una zona específica. */
    public void asignarZona(String zonaId) {
        this.zonaAsignadaId = zonaId;
    }

    /** Verifica si el operador gestiona la zona indicada. */
    public boolean gestionaZona(String zonaId) {
        return zonaId != null && zonaId.equals(this.zonaAsignadaId);
    }

    /** Desvincula al operador de su zona actual. */
    public void removerZona() {
        this.zonaAsignadaId = null;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getZonaAsignadaId() {
        return zonaAsignadaId;
    }

    public void setZonaAsignadaId(String zonaAsignadaId) {
        this.zonaAsignadaId = zonaAsignadaId;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Compares this Operador object with another object for equality.
     * 
     * Two Operador objects are considered equal if they have the same id.
     * This method performs the following checks:
     * - Returns true if both references point to the same object (identity check)
     * - Returns false if the compared object is null or not an instance of Operador class
     * - Returns true only if both objects have the same id value
     * 
     * @param o the object to be compared with this Operador
     * @return true if this Operador has the same id as the compared object, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Operador operador = (Operador) o;
        return id.equals(operador.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Operador{id='" + id + "', nombre='" + nombre + "', zona='" + zonaAsignadaId + "'}";
    }
}
