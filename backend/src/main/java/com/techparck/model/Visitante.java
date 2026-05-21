package main.java.com.techparck.model;

import main.java.com.techparck.structures.ListaEnlazada;
import main.java.com.techparck.structures.SetPropio;

//── Atributos, Constructor, Getters y Setters ────────────────────────────────────────────
public class Visitante {
    private String id;
    private String nombre; 
    private String documento; 
    private int edad; 
    private double altura; 
    private String fotoPaseUrl;
    private double saldoVirtual; 
    private Ticket ticketActivo; 
    private ListaEnlazada<String> historialVisitas;
    private SetPropio<String> favoritos; /** Atracciones favoritas del visitante:  set propio de IDs. */

    public Visitante() {
        this.historialVisitas = new ListaEnlazada<>();
        this.favoritos        = new SetPropio<>();
    }

    public Visitante(String id, String nombre, String documento, int edad, double altura) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.edad = edad;
        this.altura = altura;
        this.saldoVirtual = 0.0; 
        this.historialVisitas = new ListaEnlazada<>();
        this.favoritos        = new SetPropio<>();
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

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public String getFotoPaseUrl() {
        return fotoPaseUrl;
    }

    public void setFotoPaseUrl(String fotoPaseUrl) {
        this.fotoPaseUrl = fotoPaseUrl;
    }

    public double getSaldoVirtual() {
        return saldoVirtual;
    }

    public void setSaldoVirtual(double saldoVirtual) {
        this.saldoVirtual = saldoVirtual;
    }

    public Ticket getTicketActivo() {
        return ticketActivo;
    }

    public void setTicketActivo(Ticket ticketActivo) {
        this.ticketActivo = ticketActivo;
    }

    public ListaEnlazada<String> getHistorialVisitas() {
        return historialVisitas;
    }

    public void setHistorialVisitas(ListaEnlazada<String> historialVisitas) {
        this.historialVisitas = historialVisitas;
    }

    public SetPropio<String> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(SetPropio<String> favoritos) {
        this.favoritos = favoritos;
    }
     
    // ── Métodos de negocio ────────────────────────────────────

    /** Registra la visita a una atracción en el historial. */
    public void registrarVisita(String idAtraccion) {
        historialVisitas.agregar(idAtraccion);
    }

    public boolean agregarFavorito(String idAtraccion) {
        return favoritos.agregar(idAtraccion);
    }

    public boolean eliminarFavorito(String idAtraccion) {
        return favoritos.eliminar(idAtraccion);
    }

    public boolean esFavorito(String idAtraccion) {
        return favoritos.contiene(idAtraccion);
    }

    public void recargarSaldo(double monto) {
        if (monto > 0) this.saldoVirtual += monto;
    }

    /** Descuenta saldo virtual si tiene suficiente. Retorna true si se pudo. */
    public boolean descontarSaldo(double monto) {
        if (saldoVirtual >= monto) {
            saldoVirtual -= monto;
            return true;
        }
        return false;
    }

    /** Verifica si tiene ticket activo con tipo FastPass. */
    public boolean tieneFastPass() {
        return ticketActivo != null && ticketActivo.esFastPass();
    }

    /** Prioridad del visitante en la cola (1=FastPass, 2=General). */
    public int getPrioridad() {
        return ticketActivo != null ? ticketActivo.getPrioridad() : 2;
    }
    
}
