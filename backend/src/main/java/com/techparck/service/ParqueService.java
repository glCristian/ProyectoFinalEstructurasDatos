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

}
