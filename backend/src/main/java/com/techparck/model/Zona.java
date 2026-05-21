package main.java.com.techparck.model;

//── Atributos, Constructor, Getters, Setters y toString ────────────────────────────────────
public class Zona {
    private String id;
    private String nombre;
    private int capacidadMaxima;
    private int visitantesActuales;
    //Falta crear ListaEnlazada<>

    public Zona() {
        //Falta
    }

    public Zona(String id, String nombre, int capacidadMaxima) {
        this.id               = id;
        this.nombre           = nombre;
        this.capacidadMaxima  = capacidadMaxima;
        this.visitantesActuales = 0;
        //Falta 
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

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public int getVisitantesActuales() {
        return visitantesActuales;
    }

    public void setVisitantesActuales(int visitantesActuales) {
        this.visitantesActuales = visitantesActuales;
    }

    @Override
    public String toString() {
        return "Zona{id='" + id + "', nombre='" + nombre + "', visitantes=" + visitantesActuales + "/" + capacidadMaxima + "}";
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    //Falta
}
