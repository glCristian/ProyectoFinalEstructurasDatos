package com.techpark.model;

/**
 * Representa al administrador del parque Tech-Park UQ.
 * Puede gestionar empleados, zonas, atracciones y consultar reportes.
 */
public class Administrador {

    private String id;
    private String nombre;
    private String documento;
    private String passwordHash;   // contraseña de acceso al panel de administración

    // ── Constructores ──────────────────────────────────────────────────

    public Administrador() {}

    public Administrador(String id, String nombre, String documento, String passwordHash) {
        this.id          = id;
        this.nombre      = nombre;
        this.documento   = documento;
        this.passwordHash = passwordHash;
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    /** Verifica la clave de acceso. */
    public boolean autenticar(String clave) {
        return this.passwordHash != null && this.passwordHash.equals(clave);
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return "Administrador{id='" + id + "', nombre='" + nombre + "'}";
    }
}
