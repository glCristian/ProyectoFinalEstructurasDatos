package com.techpark.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el ticket de ingreso de un visitante al parque.
 */
public class Ticket {

    private String id;
    private TipoTicket tipo;
    private String visitanteId;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private boolean activo;
    private double precio;
    private int numeroPersonasIncluidas;
    private List<Visitante> miembrosDeLaFamilia;
    private double precioCompra;
    private LocalDateTime fechaCompra;

    // ── Constructores ──────────────────────────────────────────────────

    public Ticket() {
        this.miembrosDeLaFamilia = new ArrayList<>();
    }

    public Ticket(String id, TipoTicket tipo, String visitanteId, double precio) {
        this(id, tipo, visitanteId, precio, 1, null);
    }

    public Ticket(String id, TipoTicket tipo, String visitanteId, double precioCompra, int numeroPersonasIncluidas, List<Visitante> miembrosDeLaFamilia) {
        this.id               = id;
        this.tipo             = tipo;
        this.visitanteId      = visitanteId;
        this.precio           = precioCompra;
        this.precioCompra     = precioCompra;
        this.numeroPersonasIncluidas = Math.max(1, numeroPersonasIncluidas);
        this.miembrosDeLaFamilia = miembrosDeLaFamilia != null ? new ArrayList<>(miembrosDeLaFamilia) : new ArrayList<>();
        this.fechaEmision     = LocalDate.now();
        this.fechaVencimiento = LocalDate.now();
        this.activo           = true;
        this.fechaCompra      = LocalDateTime.now();
    }

    // ── Métodos de negocio ────────────────────────────────────────────

    public boolean esFastPass() {
        return tipo == TipoTicket.FAST_PASS;
    }

    public boolean esGeneral() {
        return tipo == TipoTicket.GENERAL;
    }

    public boolean esFamiliar() {
        return tipo == TipoTicket.FAMILIAR;
    }

    public void desactivar() {
        this.activo = false;
    }

    /** Prioridad numérica para la cola: 1 = FastPass, 2 = General/Familiar. */
    public int getPrioridad() {
        return tipo == TipoTicket.FAST_PASS ? 1 : 2;
    }

    public int getNumeroPersonasIncluidas() { return numeroPersonasIncluidas; }
    public void setNumeroPersonasIncluidas(int numeroPersonasIncluidas) { this.numeroPersonasIncluidas = Math.max(1, numeroPersonasIncluidas); }

    public List<Visitante> getMiembrosDeLaFamilia() {
        if (miembrosDeLaFamilia == null) miembrosDeLaFamilia = new ArrayList<>();
        return miembrosDeLaFamilia;
    }

    public void setMiembrosDeLaFamilia(List<Visitante> miembrosDeLaFamilia) {
        this.miembrosDeLaFamilia = miembrosDeLaFamilia != null ? new ArrayList<>(miembrosDeLaFamilia) : new ArrayList<>();
    }

    public double getPrecioCompra() {
        return precioCompra > 0 ? precioCompra : precio;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
        this.precio = precioCompra;
    }

    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }

    // ── Getters / Setters ──────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TipoTicket getTipo() { return tipo; }
    public void setTipo(TipoTicket tipo) { this.tipo = tipo; }

    public String getVisitanteId() { return visitanteId; }
    public void setVisitanteId(String visitanteId) { this.visitanteId = visitanteId; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public double getPrecio() { return getPrecioCompra(); }
    public void setPrecio(double precio) { setPrecioCompra(precio); }
}
