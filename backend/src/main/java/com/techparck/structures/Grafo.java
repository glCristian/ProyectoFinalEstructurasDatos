package main.java.com.techparck.structures;

import java.util.*;

public class Grafo {

    // ── Arista ────────────────────────────────────────────────────────
    public static class Arista {
        public final String destino;
        public final double peso;

        public Arista(String destino, double peso) {
            this.destino = destino;
            this.peso    = peso;
        }

        @Override
        public String toString() {
            return "→" + destino + "(" + peso + ")";
        }
    }

    // ── Nodo del grafo ────────────────────────────────────────────────
    public static class NodoGrafo {
        public final String id;
        private final List<Arista> vecinos;

        public NodoGrafo(String id) {
            this.id      = id;
            this.vecinos = new ArrayList<>();
        }

        public List<Arista> getVecinos() { return vecinos; }

        public void agregarVecino(String destino, double peso) {
            vecinos.add(new Arista(destino, peso));
        }

        /** Elimina la arista hacia destino. */
        public boolean eliminarVecino(String destino) {
            return vecinos.removeIf(a -> a.destino.equals(destino));
        }
    }

    // ── Atributos ─────────────────────────────────────────────────────
    private final Map<String, NodoGrafo> nodos = new LinkedHashMap<>();

    // ── Gestión de nodos ──────────────────────────────────────────────

    /** Agrega un nodo al grafo (idAtraccion). */
    public void agregarNodo(String id) {
        nodos.putIfAbsent(id, new NodoGrafo(id));
    }

    /** Elimina un nodo y todas sus aristas. */
    public void eliminarNodo(String id) {
        nodos.remove(id);
        for (NodoGrafo n : nodos.values()) n.eliminarVecino(id);
    }

    /** Retorna true si el nodo existe. */
    public boolean existeNodo(String id) {
        return nodos.containsKey(id);
    }

    /** Retorna todos los IDs de nodos. */
    public Set<String> getNodos() {
        return nodos.keySet();
    }

    /** Retorna el NodoGrafo con el ID indicado. */
    public NodoGrafo getNodo(String id) {
        return nodos.get(id);
    }

    // ── Gestión de aristas ────────────────────────────────────────────

    /**
     * Conecta dos nodos con una arista bidireccional.
     * Si los nodos no existen, los crea automáticamente.
     */
    public void conectar(String idA, String idB, double peso) {
        agregarNodo(idA);
        agregarNodo(idB);
        nodos.get(idA).agregarVecino(idB, peso);
        nodos.get(idB).agregarVecino(idA, peso);
    }

    /** Desconecta dos nodos (bidireccional). */
    public void desconectar(String idA, String idB) {
        if (nodos.containsKey(idA)) nodos.get(idA).eliminarVecino(idB);
        if (nodos.containsKey(idB)) nodos.get(idB).eliminarVecino(idA);
    }

    /** Retorna los vecinos de un nodo. */
    public List<Arista> getVecinos(String id) {
        NodoGrafo n = nodos.get(id);
        return n != null ? n.getVecinos() : Collections.emptyList();
    }
    
    // ── Algoritmos ────────────────────────────────────────────────────

    /**
     * BFS desde el nodo origen.
     * Retorna la lista de IDs en orden de visita.
     */
    public List<String> bfs(String origen) {
        List<String>   visitados = new ArrayList<>();
        Set<String>    visto     = new HashSet<>();
        Queue<String>  cola      = new LinkedList<>();

        if (!nodos.containsKey(origen)) return visitados;

        cola.add(origen);
        visto.add(origen);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            visitados.add(actual);
            for (Arista a : getVecinos(actual)) {
                if (!visto.contains(a.destino)) {
                    visto.add(a.destino);
                    cola.add(a.destino);
                }
            }
        }
        return visitados;
    }

    /**
     * Dijkstra desde el nodo origen hacia el destino.
     * Retorna la lista de IDs que forman el camino más corto,
     * o una lista vacía si no existe camino.
     */
    public List<String> dijkstra(String origen, String destino) {
        if (!nodos.containsKey(origen) || !nodos.containsKey(destino)) {
            return Collections.emptyList();
        }

        // dist → distancia mínima desde origen
        Map<String, Double>  dist   = new HashMap<>();
        Map<String, String>  previo = new HashMap<>();
        Set<String>          visto  = new HashSet<>();

        // Cola de prioridad: [id, distancia]
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e[1]));
        // Usamos PriorityQueue de Java solo como estructura auxiliar de algoritmo;
        // la ColaPrioridad propia gestiona la lógica de negocio de visitantes.

        for (String id : nodos.keySet()) dist.put(id, Double.MAX_VALUE);
        dist.put(origen, 0.0);
        pq.offer(new double[]{0, 0}); // dummy para iniciar
        Map<String, Double> distAux = new HashMap<>();
        distAux.put(origen, 0.0);

        // Cola simplificada con mapa
        Queue<String> pendientes = new LinkedList<>();
        pendientes.add(origen);

        while (!pendientes.isEmpty()) {
            // Seleccionar el nodo no visitado con menor distancia
            String actual = null;
            double menorDist = Double.MAX_VALUE;
            for (String n : dist.keySet()) {
                if (!visto.contains(n) && dist.get(n) < menorDist) {
                    menorDist = dist.get(n);
                    actual    = n;
                }
            }
            if (actual == null || actual.equals(destino)) break;
            visto.add(actual);

            for (Arista arista : getVecinos(actual)) {
                if (!visto.contains(arista.destino)) {
                    double nuevaDist = dist.get(actual) + arista.peso;
                    if (nuevaDist < dist.get(arista.destino)) {
                        dist.put(arista.destino, nuevaDist);
                        previo.put(arista.destino, actual);
                        pendientes.add(arista.destino);
                    }
                }
            }
        }

        // Reconstruir camino
        if (dist.get(destino) == Double.MAX_VALUE) return Collections.emptyList();

        List<String> camino = new ArrayList<>();
        String cursor = destino;
        while (cursor != null) {
            camino.add(0, cursor);
            cursor = previo.get(cursor);
        }
        return camino;
    }

    /** Distancia total del camino calculado por Dijkstra. */
    public double distanciaDijkstra(String origen, String destino) {
        List<String> camino = dijkstra(origen, destino);
        if (camino.isEmpty()) return Double.MAX_VALUE;
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            String a = camino.get(i), b = camino.get(i + 1);
            for (Arista ar : getVecinos(a)) {
                if (ar.destino.equals(b)) { total += ar.peso; break; }
            }
        }
        return total;
    }

    /** Retorna true si existe al menos un camino entre los dos nodos (BFS). */
    public boolean estanConectados(String idA, String idB) {
        return bfs(idA).contains(idB);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Grafo:\n");
        for (Map.Entry<String, NodoGrafo> e : nodos.entrySet()) {
            sb.append("  ").append(e.getKey()).append(" → ").append(e.getValue().getVecinos()).append("\n");
        }
        return sb.toString();
    }
}