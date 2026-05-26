package com.techpark.model;

import com.techpark.structures.ListaEnlazada;

/**
 * Representa una zona del parque Tech-Park UQ.
 * Agrupa varias atracciones y tiene operadores asignados.
 */
public class Zona {

    private String id;
    private String nombre;
    private int capacidadMaxima;
    private int visitantesActuales;

    /** Lista enlazada propia de IDs de atracciones pertenecientes a esta zona. */
    private ListaEnlazada<String> idsAtracciones;

    /** Lista enlazada propia de IDs de operadores asignados a esta zona. */
    private ListaEnlazada<String> idsOperadores;

    // ── Constructores ──────────────────────────────────────────────────

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

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    /** Agrega una atracción a la zona. */
    public void agregarAtraccion(String idAtraccion) {
        idsAtracciones.agregar(idAtraccion);
    }

    /** Remueve una atracción de la zona. */
    public boolean removerAtraccion(String idAtraccion) {
        return idsAtracciones.eliminar(idAtraccion);
    }

    /** Asigna un operador a la zona. */
    public void asignarOperador(String idOperador) {
        idsOperadores.agregar(idOperador);
    }

    /** Remueve un operador de la zona. */
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

    /** Decrementa el contador de visitantes actuales. */
    public void retirarVisitante() {
        if (visitantesActuales > 0) visitantesActuales--;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public int getVisitantesActuales() { return visitantesActuales; }
    public void setVisitantesActuales(int visitantesActuales) { this.visitantesActuales = visitantesActuales; }

    public ListaEnlazada<String> getIdsAtracciones() { return idsAtracciones; }
    public void setIdsAtracciones(ListaEnlazada<String> idsAtracciones) { this.idsAtracciones = idsAtracciones; }

    public ListaEnlazada<String> getIdsOperadores() { return idsOperadores; }
    public void setIdsOperadores(ListaEnlazada<String> idsOperadores) { this.idsOperadores = idsOperadores; }

    @Override
    public String toString() {
        return "Zona{id='" + id + "', nombre='" + nombre + "', visitantes=" + visitantesActuales + "/" + capacidadMaxima + "}";
    }
}
