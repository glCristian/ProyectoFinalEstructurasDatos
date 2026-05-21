package main.java.com.techparck.model;

//── Atributos, Constructor, Getters, Setters y toString ────────────────────────────────────
public class Operador {
    private String id;
    private String nombre;
    private String documento;
    private String zonaAsignadaId;  // zona a la que fue asignado (solo una)
    private boolean activo;

    public Operador() {}

    public Operador(String id, String nombre, String documento) {
        this.id        = id;
        this.nombre    = nombre;
        this.documento = documento;
        this.activo    = true;
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

    @Override
    public String toString() {
        return "Operador{id='" + id + "', nombre='" + nombre + "', zona='" + zonaAsignadaId + "'}";
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    public void asignarZona(String zonaId) {
        this.zonaAsignadaId = zonaId;
    }

    public boolean gestionaZona(String zonaId) {
        return zonaId != null && zonaId.equals(this.zonaAsignadaId);
    }

    public void removerZona() {
        this.zonaAsignadaId = null;
    }

}
