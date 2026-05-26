package test.java.com.techpark;

import com.techpark.model.*;
import com.techpark.service.ParqueService;
import com.techpark.structures.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TechParkTest {
    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 1: Lista Enlazada
    // ────────────────────────────────────────────────────────────────
    @Test @Order(1)
    @DisplayName("ListaEnlazada: agregar, contener, eliminar y tamaño")
    void testListaEnlazada() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        assertTrue(lista.estaVacia());

        lista.agregar("A1");
        lista.agregar("A2");
        lista.agregar("A3");

        assertEquals(3, lista.tamanio());
        assertTrue(lista.contiene("A2"));
        assertFalse(lista.contiene("A9"));

        assertTrue(lista.eliminar("A2"));
        assertEquals(2, lista.tamanio());
        assertFalse(lista.contiene("A2"));

        assertEquals("A1", lista.obtener(0));
        assertEquals("A3", lista.obtener(1));
    }

    @Test @Order(2)
    @DisplayName("SetPropio: sin duplicados, agregar y eliminar")
    void testSetPropio() {
        SetPropio<String> set = new SetPropio<>();
        assertTrue(set.estaVacio());

        assertTrue(set.agregar("A1"));
        assertTrue(set.agregar("A2"));
        assertFalse(set.agregar("A1")); // duplicado

        assertEquals(2, set.tamanio());
        assertTrue(set.contiene("A1"));
        assertFalse(set.contiene("A9"));

        assertTrue(set.eliminar("A1"));
        assertFalse(set.contiene("A1"));
        assertEquals(1, set.tamanio());
    }

    @Test @Order(3)
    @DisplayName("ColaPrioridad: FastPass (p=1) sale antes que General (p=2)")
    void testColaPrioridad() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();

        cola.insertar("Visitante-General-1",  2);
        cola.insertar("Visitante-General-2",  2);
        cola.insertar("Visitante-FastPass-1", 1); // prioridad alta
        cola.insertar("Visitante-General-3",  2);
        cola.insertar("Visitante-FastPass-2", 1); // prioridad alta

        assertEquals(5, cola.tamanio());

        // Los FastPass deben salir primero, y en orden FIFO
        assertEquals("Visitante-FastPass-1", cola.extraer());
        assertEquals("Visitante-FastPass-2", cola.extraer());
        // Luego los General en orden de inserción
        assertEquals("Visitante-General-1",  cola.extraer());
        assertEquals("Visitante-General-2",  cola.extraer());
        assertEquals("Visitante-General-3",  cola.extraer());

        assertTrue(cola.estaVacia());
    }

    @Test @Order(4)
    @DisplayName("ABB: insertar, buscar, inorden y eliminar")
    void testABB() {
        ABB<String, Integer> abb = new ABB<>();

        abb.insertar("montaña rusa", 3);
        abb.insertar("caída libre",  5);
        abb.insertar("tobogán",      1);
        abb.insertar("carrusel",     2);

        assertEquals(4, abb.tamanio());
        assertEquals(3,  abb.buscar("montaña rusa"));
        assertNull(abb.buscar("fantasma")); // no existe

        // inorden → orden alfabético
        List<Integer> inorden = abb.inorden();
        assertEquals(List.of(5, 2, 3, 1), inorden); // c < m < t (en español)

        abb.eliminar("montaña rusa");
        assertFalse(abb.contiene("montaña rusa"));
        assertEquals(3, abb.tamanio());
    }

    @Test @Order(5)
    @DisplayName("Grafo: BFS alcanza todos los nodos y Dijkstra encuentra camino mínimo")
    void testGrafo() {
        Grafo grafo = new Grafo();
        grafo.conectar("A1", "A2", 10);
        grafo.conectar("A2", "A3", 5);
        grafo.conectar("A1", "A3", 20);
        grafo.conectar("A3", "A4", 3);

        // BFS desde A1 debe visitar todos
        List<String> bfs = grafo.bfs("A1");
        assertEquals(4, bfs.size());
        assertTrue(bfs.contains("A4"));

        // Dijkstra A1→A4: camino mínimo es A1→A2→A3→A4 = 18
        List<String> camino = grafo.dijkstra("A1", "A4");
        assertFalse(camino.isEmpty());
        assertEquals("A1", camino.get(0));
        assertEquals("A4", camino.get(camino.size() - 1));
        assertEquals(18.0, grafo.distanciaDijkstra("A1", "A4"), 0.001);

        // Nodo desconectado
        grafo.agregarNodo("AISLADO");
        assertFalse(grafo.estanConectados("A1", "AISLADO"));
    }
}
