package com.techpark.model;

import com.techpark.structures.ListaEnlazada;
import com.techpark.structures.SetPropio;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un visitante del parque Tech-Park UQ.
 * Contiene perfil personal, saldo virtual, historial de visitas y favoritos.
 */
public class Visitante {

    // ── Identificación y perfil ────────────────────────────────────────
    private String id;
    private String nombre;
    private String documento;          // número de documento de identidad
    private int    edad;               // en años
    private double altura;             // en metros
    private String fotoPaseUrl;        // opcional, URL de la fotografía del pase digital
    private String username;
    private String passwordHash;



    // ── Saldo y ticket ────────────────────────────────────────────────
    private double saldoVirtual;
    private Ticket ticketComprado;
    private Ticket ticketActivo;
    private boolean puedeDentrar;
    private transient List<Visitante> miembrosGrupoFamiliar;
    private int numeroPersonasFamilia;
    private transient Visitante liderGrupo;

    // ── Estructuras propias ───────────────────────────────────────────
    /** Historial de visitas: lista enlazada de IDs de atracciones visitadas. */
    private ListaEnlazada<String> historialVisitas;

    /** Atracciones favoritas del visitante: set propio de IDs. */
    private SetPropio<String> favoritos;

    // ── Constructores ──────────────────────────────────────────────────

    public Visitante() {
        this.historialVisitas = new ListaEnlazada<>();
        this.favoritos        = new SetPropio<>();
        this.miembrosGrupoFamiliar = new ArrayList<>();
    }

    public Visitante(String id, String nombre, String documento, int edad, double altura, String username, String passwordHash) {
        this.id               = id;
        this.nombre           = nombre;
        this.documento        = documento;
        this.edad             = edad;
        this.altura           = altura;
        this.saldoVirtual     = 0.0;
        this.historialVisitas = new ListaEnlazada<>();
        this.favoritos        = new SetPropio<>();
        this.username         = username;
        this.passwordHash     = passwordHash;
        this.miembrosGrupoFamiliar = new ArrayList<>();

    }

    public Visitante(String id, String nombre, String documento, int edad, double altura) {
        this(id, nombre, documento, edad, altura, null, null);
    }

    // ── Métodos de negocio (firmas) ────────────────────────────────────

    /** Registra la visita a una atracción en el historial. */
    public void registrarVisita(String idAtraccion) {
        historialVisitas.agregar(idAtraccion);
    }

    /** Agrega una atracción a favoritos. */
    public boolean agregarFavorito(String idAtraccion) {
        return favoritos.agregar(idAtraccion);
    }

    /** Elimina una atracción de favoritos. */
    public boolean eliminarFavorito(String idAtraccion) {
        return favoritos.eliminar(idAtraccion);
    }

    /** Verifica si una atracción está en favoritos. */
    public boolean esFavorito(String idAtraccion) {
        return favoritos.contiene(idAtraccion);
    }

    /** Recarga saldo virtual. */
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
        Ticket ticket = getTicketComprado();
        return ticket != null && ticket.esFastPass() && ticket.isActivo();
    }

    /** Prioridad del visitante en la cola (1=FastPass, 2=General). */
    public int getPrioridad() {
        Ticket ticket = getTicketComprado();
        return ticket != null && ticket.isActivo() ? ticket.getPrioridad() : 2;
    }

    public boolean tieneTicketValido() {
        Ticket ticket = getTicketComprado();
        return puedeDentrar && ticket != null && ticket.isActivo();
    }

    public void sincronizarEstadoTicket() {
        Ticket ticket = resolverTicketCanonico();
        this.ticketComprado = ticket;
        this.ticketActivo = ticket;
        this.puedeDentrar = ticket != null && ticket.isActivo();
    }

    public Ticket getTicketComprado() {
        return resolverTicketCanonico();
    }

    public void setTicketComprado(Ticket ticketComprado) {
        this.ticketComprado = ticketComprado;
        this.ticketActivo = ticketComprado;
        this.puedeDentrar = ticketComprado != null && ticketComprado.isActivo();
    }

    private Ticket resolverTicketCanonico() {
        if (ticketComprado == null) return ticketActivo;
        if (ticketActivo == null) return ticketComprado;
        if (ticketActivo.isActivo() && !ticketComprado.isActivo()) return ticketActivo;
        if (ticketComprado.isActivo() && !ticketActivo.isActivo()) return ticketComprado;
        if (esTicketMasCompleto(ticketActivo, ticketComprado)) return ticketActivo;
        return ticketComprado;
    }

    private boolean esTicketMasCompleto(Ticket candidato, Ticket referencia) {
        if (candidato == null) return false;
        if (referencia == null) return true;

        int personasCandidato = candidato.getNumeroPersonasIncluidas();
        int personasReferencia = referencia.getNumeroPersonasIncluidas();
        if (personasCandidato != personasReferencia) {
            return personasCandidato > personasReferencia;
        }

        double precioCandidato = candidato.getPrecioCompra();
        double precioReferencia = referencia.getPrecioCompra();
        if (precioCandidato != precioReferencia) {
            return precioCandidato > precioReferencia;
        }

        int miembrosCandidato = candidato.getMiembrosDeLaFamilia() != null ? candidato.getMiembrosDeLaFamilia().size() : 0;
        int miembrosReferencia = referencia.getMiembrosDeLaFamilia() != null ? referencia.getMiembrosDeLaFamilia().size() : 0;
        return miembrosCandidato > miembrosReferencia;
    }

    // ── Getters / Setters ──────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public double getAltura() { return altura; }
    public void setAltura(double altura) { this.altura = altura; }

    public String getFotoPaseUrl() { return fotoPaseUrl; }
    public void setFotoPaseUrl(String fotoPaseUrl) { this.fotoPaseUrl = fotoPaseUrl; }

    public double getSaldoVirtual() { return saldoVirtual; }
    public void setSaldoVirtual(double saldoVirtual) { this.saldoVirtual = saldoVirtual; }

    public Ticket getTicketActivo() { return ticketActivo; }
    public void setTicketActivo(Ticket ticketActivo) { setTicketComprado(ticketActivo); }

    public boolean isPuedeDentrar() { return puedeDentrar; }
    public void setPuedeDentrar(boolean puedeDentrar) { this.puedeDentrar = puedeDentrar; }

    public List<Visitante> getMiembrosGrupoFamiliar() {
        if (miembrosGrupoFamiliar == null) miembrosGrupoFamiliar = new ArrayList<>();
        return miembrosGrupoFamiliar;
    }

    public void setMiembrosGrupoFamiliar(List<Visitante> miembrosGrupoFamiliar) {
        this.miembrosGrupoFamiliar = miembrosGrupoFamiliar != null ? miembrosGrupoFamiliar : new ArrayList<>();
    }

    public int getNumeroPersonasFamilia() { return numeroPersonasFamilia; }
    public void setNumeroPersonasFamilia(int numeroPersonasFamilia) { this.numeroPersonasFamilia = numeroPersonasFamilia; }

    public Visitante getLiderGrupo() { return liderGrupo; }
    public void setLiderGrupo(Visitante liderGrupo) { this.liderGrupo = liderGrupo; }

    public ListaEnlazada<String> getHistorialVisitas() { return historialVisitas; }
    public void setHistorialVisitas(ListaEnlazada<String> historialVisitas) { this.historialVisitas = historialVisitas; }

    public SetPropio<String> getFavoritos() { return favoritos; }
    public void setFavoritos(SetPropio<String> favoritos) { this.favoritos = favoritos; }

    


    @Override
    public String toString() {
        return "Visitante{id='" + id + "', nombre='" + nombre + "', saldo=" + saldoVirtual + "}";
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
}
