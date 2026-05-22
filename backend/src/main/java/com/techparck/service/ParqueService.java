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
 
}
