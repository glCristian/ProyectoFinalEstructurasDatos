package main.java.com.techparck.model;

import java.time.LocalDate;

//── Atributos, Constructor, Getters y Setters ────────────────────────────────────────────
public class Ticket {
    private String id; 
    private TipoTicket tipo; 
    private String visitanteId; 
    private LocalDate fechaEmision; 
    private LocalDate fechaVencimiento; 
    private boolean activo; 
    private double precio;
    
    public Ticket(String id, TipoTicket tipo, String visitanteId, LocalDate fechaEmision, LocalDate fechaVencimiento,
            boolean activo, double precio) {
        this.id = id;
        this.tipo = tipo;
        this.visitanteId = visitanteId;
        this.fechaEmision = LocalDate.now();
        this.fechaVencimiento = fechaVencimiento;
        this.activo = activo;
        this.precio = precio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoTicket getTipo() {
        return tipo;
    }

    public void setTipo(TipoTicket tipo) {
        this.tipo = tipo;
    }

    public String getVisitanteId() {
        return visitanteId;
    }

    public void setVisitanteId(String visitanteId) {
        this.visitanteId = visitanteId;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    } 

    //── Métodos de negocio ────────────────────────────────────────────
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

} 
