package main.java.com.techparck.service;

import main.java.com.techparck.model.*;
import main.java.com.techparck.structures.*;

import java.util.*;

/**
 * Servicio principal del parque Tech-Park UQ.
 * Actúa como repositorio central en memoria y orquesta la lógica de negocio.
 */
public class ParqueService {
    // ── Singleton ─────────────────────────────────────────────────────
    private static ParqueService instancia;

    public static ParqueService getInstance() {
        if (instancia == null) instancia = new ParqueService();
        return instancia;
    }

    // ── Repositorios en memoria ────────────────────────────────────────
    private final Map<String, Atraccion> atracciones = new LinkedHashMap<>();
    private final Map<String, Zona> zonas = new LinkedHashMap<>();
    private final Map<String, Visitante> visitantes = new LinkedHashMap<>();
    private final Map<String, Operador> operadores = new LinkedHashMap<>();
    private final Map<String, Administrador> admins = new LinkedHashMap<>();

    /** Cola de prioridad por atracción: id_atraccion → cola de visitantes */
    private final Map<String, ColaPrioridad<Visitante>> colasPorAtraccion = new HashMap<>();

    /** ABB de atracciones indexado por nombre */
    private final ABB<String, Atraccion> arbolAtracciones = new ABB<>();

    /** Grafo del parque */
    private final Grafo grafo = new Grafo();

    /** Estado climático actual */
    private boolean alertaClimatica = false;
    private String  tipoAlerta      = "";

    // ── Capacidad total del parque ─────────────────────────────────────
    private int capacidadMaximaParque = 1000;
    private int visitantesActualesParque = 0;

    // ── Constructor privado ────────────────────────────────────────────
    private ParqueService() {}
    
        // ────────────────────────────────────────────────────────────────
    //  ATRACCIONES
    // ────────────────────────────────────────────────────────────────

    /** Registra una nueva atracción en el sistema. */
    public boolean agregarAtraccion(Atraccion a) {
        if (atracciones.containsKey(a.getId())) return false;
        atracciones.put(a.getId(), a);
        arbolAtracciones.insertar(a.getNombre().toLowerCase(), a);
        grafo.agregarNodo(a.getId());
        colasPorAtraccion.put(a.getId(), new ColaPrioridad<>());
        return true;
    }

    /** Retorna una atracción por ID. */
    public Atraccion getAtraccion(String id) { return atracciones.get(id); }

    /** Retorna todas las atracciones. */
    public Collection<Atraccion> getAtracciones() { return atracciones.values(); }

    /** Busca una atracción por nombre (ABB). */
    public Atraccion buscarAtraccionPorNombre(String nombre) {
        return arbolAtracciones.buscar(nombre.toLowerCase());
    }

    /** Actualiza el estado de una atracción. */
    public boolean actualizarEstadoAtraccion(String id, EstadoAtraccion estado, String motivo) {
        Atraccion a = atracciones.get(id);
        if (a == null) return false;
        a.setEstado(estado);
        a.setMotivoCierre(estado == EstadoAtraccion.ACTIVA ? null : motivo);
        return true;
    }

    /** Elimina una atracción. */
    public boolean eliminarAtraccion(String id) {
        Atraccion a = atracciones.remove(id);
        if (a == null) return false;
        arbolAtracciones.eliminar(a.getNombre().toLowerCase());
        grafo.eliminarNodo(id);
        colasPorAtraccion.remove(id);
        return true;
    }

    // ────────────────────────────────────────────────────────────────
    //  ZONAS
    // ────────────────────────────────────────────────────────────────

    public boolean agregarZona(Zona z) {
        if (zonas.containsKey(z.getId())) return false;
        zonas.put(z.getId(), z);
        return true;
    }

    public Zona getZona(String id) { return zonas.get(id); }
    public Collection<Zona> getZonas() { return zonas.values(); }

    public boolean eliminarZona(String id) {
        return zonas.remove(id) != null;
    }

    // ────────────────────────────────────────────────────────────────
    //  VISITANTES
    // ────────────────────────────────────────────────────────────────

    /** Registra un visitante y emite su ticket. */
    public boolean registrarVisitante(Visitante v, TipoTicket tipoTicket, double precioTicket) {
        if (visitantes.containsKey(v.getId())) return false;
        if (visitantesActualesParque >= capacidadMaximaParque) return false; // aforo completo
        Ticket t = new Ticket("T-" + v.getId(), tipoTicket, v.getId(), precioTicket);
        v.setTicketActivo(t);
        visitantes.put(v.getId(), v);
        visitantesActualesParque++;
        return true;
    }

    public Visitante getVisitante(String id) { return visitantes.get(id); }
    public Collection<Visitante> getVisitantes() { return visitantes.values(); }

    /** Retira un visitante del parque. */
    public boolean retirarVisitante(String id) {
        if (visitantes.remove(id) != null) {
            if (visitantesActualesParque > 0) visitantesActualesParque--;
            return true;
        }
        return false;
    }

    // ────────────────────────────────────────────────────────────────
    //  OPERADORES
    // ────────────────────────────────────────────────────────────────

    public boolean agregarOperador(Operador op) {
        if (operadores.containsKey(op.getId())) return false;
        operadores.put(op.getId(), op);
        return true;
    }

    public Operador getOperador(String id) { return operadores.get(id); }
    public Collection<Operador> getOperadores() { return operadores.values(); }

    /**
     * Asigna un operador a una zona.
     * Garantiza que no haya conflictos y que la atracción no quede sin operador.
     */
    public boolean asignarOperadorAZona(String operadorId, String zonaId) {
        Operador op = operadores.get(operadorId);
        Zona     z  = zonas.get(zonaId);
        if (op == null || z == null) return false;

        // Remover de zona anterior si existía
        if (op.getZonaAsignadaId() != null) {
            Zona zonaAnterior = zonas.get(op.getZonaAsignadaId());
            if (zonaAnterior != null) zonaAnterior.removerOperador(operadorId);
        }
        op.asignarZona(zonaId);
        z.asignarOperador(operadorId);
        return true;
    }

    public boolean eliminarOperador(String id) {
        Operador op = operadores.remove(id);
        if (op == null) return false;
        if (op.getZonaAsignadaId() != null) {
            Zona z = zonas.get(op.getZonaAsignadaId());
            if (z != null) z.removerOperador(id);
        }
        return true;
    }

    // ────────────────────────────────────────────────────────────────
    //  ADMINISTRADORES
    // ────────────────────────────────────────────────────────────────

    public boolean agregarAdmin(Administrador admin) {
        if (admins.containsKey(admin.getId())) return false;
        admins.put(admin.getId(), admin);
        return true;
    }

    public Administrador getAdmin(String id) { return admins.get(id); }

}
