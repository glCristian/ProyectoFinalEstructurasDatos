package main.java.com.techparck.model;

import main.java.com.techparck.structures.ListaEnlazada;

//── Atributos, Constructor, Getters, Setters y toString ────────────────────────────────────
public class Zona {
    private String id;
    private String nombre;
    private int capacidadMaxima;
    private int visitantesActuales;
    private ListaEnlazada<String> idsAtracciones;
    private ListaEnlazada<String> idsOperadores;

    public Zona() {
        this.idsAtracciones = new ListaEnlazada<>();
        this.idsOperadores  = new ListaEnlazada<>();
    }

    public Zona(String id, String nombre, int capacidadMaxima) {
        this.id               = id;
        this.nombre           = nombre;
        this.capacidadMaxima  = capacidadMaxima;
        this.visitantesActuales = 0;
        this.idsAtracciones   = new ListaEnlazada<>();
        this.idsOperadores    = new ListaEnlazada<>();
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

    public ListaEnlazada<String> getIdsAtracciones() {
        return idsAtracciones;
    }

    public void setIdsAtracciones(ListaEnlazada<String> idsAtracciones) {
        this.idsAtracciones = idsAtracciones;
    }

    public ListaEnlazada<String> getIdsOperadores() {
        return idsOperadores;
    }

    public void setIdsOperadores(ListaEnlazada<String> idsOperadores) {
        this.idsOperadores = idsOperadores;
    }

    @Override
    public String toString() {
        return "Zona{id='" + id + "', nombre='" + nombre + "', visitantes=" + visitantesActuales + "/" + capacidadMaxima + "}";
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    public void agregarAtraccion(String idAtraccion) {
        idsAtracciones.agregar(idAtraccion);
    }

    public boolean removerAtraccion(String idAtraccion) {
        return idsAtracciones.eliminar(idAtraccion);
    }

    public void asignarOperador(String idOperador) {
        idsOperadores.agregar(idOperador);
    }

    public boolean removerOperador(String idOperador) {
        return idsOperadores.eliminar(idOperador);
    }

    /** Verifica si la zona tiene cupo disponible. */
    public boolean tieneCupo() {
        return visitantesActuales < capacidadMaxima;
    }

    /** Incrementa el contador de visitantes actuales. */
    public void ingresarVisitante() {
        visitantesActuales++;
    }
}
