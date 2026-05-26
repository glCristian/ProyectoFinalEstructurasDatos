package com.techpark.service;

import com.techpark.structures.Grafo;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Servicio de gestión de rutas del parque.
 * Ejecuta Dijkstra y BFS sobre el grafo de atracciones.
 */
public class RutaService {

    private final ParqueService parqueService;

    public RutaService(ParqueService parqueService) {
        this.parqueService = parqueService;
    }

    /**
     * Calcula la ruta más corta entre dos atracciones usando Dijkstra.
     *
     * @param origen   ID de la atracción de origen.
     * @param destino  ID de la atracción de destino.
     * @return lista de IDs del camino, vacía si no existe.
     */
    public List<String> rutaOptima(String origen, String destino) {
        return parqueService.getGrafo().dijkstra(origen, destino);
    }

    /**
     * Retorna la distancia total del camino más corto (Dijkstra).
     */
    public double distanciaRutaOptima(String origen, String destino) {
        return parqueService.getGrafo().distanciaDijkstra(origen, destino);
    }

    /**
     * Recorrido BFS desde un nodo origen.
     * Útil para detectar clústeres de atracciones conectadas.
     *
     * @param origen ID de la atracción de inicio.
     * @return lista de IDs en orden de visita BFS.
     */
    public List<String> recorridoBFS(String origen) {
        return parqueService.getGrafo().bfs(origen);
    }

    /**
     * Verifica si dos atracciones están conectadas en el grafo.
     */
    public boolean estanConectadas(String idA, String idB) {
        return parqueService.getGrafo().estanConectados(idA, idB);
    }

    /**
     * Construye una representación serializable del grafo (para la GUI).
     * Retorna un mapa con "nodos" (lista de IDs) y "aristas" (lista de mapas).
     */
    public Map<String, Object> obtenerGrafoParaVista() {
        Grafo grafo = parqueService.getGrafo();
        Map<String, Object> resultado = new LinkedHashMap<>();
        Map<String, com.techpark.model.Atraccion> atracciones = new LinkedHashMap<>();

        for (com.techpark.model.Atraccion atraccion : parqueService.getAtracciones()) {
            atracciones.put(atraccion.getId(), atraccion);
        }

        // Nodos
        java.util.List<Map<String, Object>> nodos = new java.util.ArrayList<>();
        for (String id : grafo.getNodos()) {
            com.techpark.model.Atraccion atraccion = atracciones.get(id);
            Map<String, Object> nodo = new LinkedHashMap<>();
            nodo.put("id", id);
            if (atraccion != null) {
                nodo.put("nombre", atraccion.getNombre());
                nodo.put("estado", atraccion.getEstado());
                nodo.put("posicionX", atraccion.getPosicionX());
                nodo.put("posicionY", atraccion.getPosicionY());
            }
            nodos.add(nodo);
        }
        resultado.put("nodos", nodos);

        // Aristas (sin duplicados)
        java.util.List<Map<String, Object>> aristas = new java.util.ArrayList<>();
        java.util.Set<String> vistas = new java.util.HashSet<>();

        for (String id : grafo.getNodos()) {
            for (Grafo.Arista arista : grafo.getVecinos(id)) {
                String clave = id.compareTo(arista.destino) < 0
                        ? id + "-" + arista.destino
                        : arista.destino + "-" + id;
                if (vistas.add(clave)) {
                    Map<String, Object> a = new LinkedHashMap<>();
                    a.put("origen", id);
                    a.put("destino", arista.destino);
                    a.put("peso", arista.peso);
                    aristas.add(a);
                }
            }
        }
        resultado.put("aristas", aristas);
        return resultado;
    }

    /**
     * Detecta clústeres (componentes conectados) del grafo usando BFS.
     */
    public Map<String, List<String>> detectarClusters() {
        Set<String> visitados = new HashSet<>();
        Map<String, List<String>> clusters = new LinkedHashMap<>();
        int numCluster = 1;

        for (String nodo : parqueService.getGrafo().getNodos()) {
            if (!visitados.contains(nodo)) {
                List<String> componente = parqueService.getGrafo().bfs(nodo);
                visitados.addAll(componente);
                clusters.put("Clúster " + numCluster++, componente);
            }
        }
        return clusters;
    }
}
