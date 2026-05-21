package main.java.com.techparck.model;

//── Atributos, Constructor, Getters, Setters y toString ────────────────────────────────────
public class Administrador {
    private String id;
    private String nombre;
    private String documento;
    private String claveAcceso;

    public Administrador() {}

    public Administrador(String id, String nombre, String documento, String claveAcceso) {
        this.id          = id;
        this.nombre      = nombre;
        this.documento   = documento;
        this.claveAcceso = claveAcceso;
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

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    @Override
    public String toString() {
        return "Administrador{id='" + id + "', nombre='" + nombre + "'}";
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────
    /** Verifica la clave de acceso. */
    public boolean autenticar(String clave) {
        return this.claveAcceso != null && this.claveAcceso.equals(clave);
    }
}
