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

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 2: Set Propio (favoritos)
    // ────────────────────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 3: Cola de Prioridad (FastPass antes que General)
    // ────────────────────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 4: ABB (árbol binario de búsqueda)
    // ────────────────────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 5: Grafo – BFS y Dijkstra
    // ────────────────────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 6: Mantenimiento preventivo (500 visitantes)
    // ────────────────────────────────────────────────────────────────
    @Test @Order(6)
    @DisplayName("Atraccion: mantenimiento automático al alcanzar 500 visitantes")
    void testMantenimientoPreventivo() {
        Atraccion a = new Atraccion("T1", "Test", TipoAtraccion.MECANICA_SUELO,
                                    10, 1.0, 5, 0, "Z1");
        assertEquals(EstadoAtraccion.ACTIVA, a.getEstado());

        // Simular 499 visitantes: no debe cambiar a mantenimiento
        for (int i = 0; i < 499; i++) a.registrarVisitante();
        assertEquals(EstadoAtraccion.ACTIVA, a.getEstado());

        // Visitante 500 → activa mantenimiento
        boolean mantenimiento = a.registrarVisitante();
        assertTrue(mantenimiento);
        assertEquals(EstadoAtraccion.EN_MANTENIMIENTO, a.getEstado());

        // Revisión técnica → vuelve a ACTIVA con contador en 0
        a.registrarRevisionTecnica();
        assertEquals(EstadoAtraccion.ACTIVA, a.getEstado());
        assertEquals(0, a.getVisitantesAcumulados());
    }

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 7: Alerta climática cierra atracciones correctas
    // ────────────────────────────────────────────────────────────────
    @Test @Order(7)
    @DisplayName("ParqueService: alerta climática cierra solo ACUATICA y MECANICA_ALTURA")
    void testAlertaClimatica() {
        // Usar una instancia limpia para el test
        ParqueService srv = ParqueService.getInstance();

        Atraccion acuatica = new Atraccion("C1", "Laguna", TipoAtraccion.ACUATICA, 20, 1.0, 5, 0, "Z1");
        Atraccion mecanica = new Atraccion("C2", "Caída",  TipoAtraccion.MECANICA_ALTURA, 12, 1.4, 14, 0, "Z2");
        Atraccion show     = new Atraccion("C3", "Show",   TipoAtraccion.SHOW, 200, 0, 0, 0, "Z4");

        srv.agregarAtraccion(acuatica);
        srv.agregarAtraccion(mecanica);
        srv.agregarAtraccion(show);

        List<String> cerradas = srv.activarAlertaClimatica("TORMENTA_ELECTRICA");

        assertTrue(cerradas.contains("C1"));
        assertTrue(cerradas.contains("C2"));
        assertFalse(cerradas.contains("C3")); // show no se cierra por clima

        assertEquals(EstadoAtraccion.CERRADA, srv.getAtraccion("C1").getEstado());
        assertEquals(EstadoAtraccion.ACTIVA,  srv.getAtraccion("C3").getEstado());

        srv.desactivarAlertaClimatica();
        assertFalse(srv.isAlertaClimatica());
    }

    // ────────────────────────────────────────────────────────────────
    //  PRUEBA 8: Visitante – favoritos e historial
    // ────────────────────────────────────────────────────────────────
    @Test @Order(8)
    @DisplayName("Visitante: historial (ListaEnlazada) y favoritos (SetPropio)")
    void testVisitanteFavoritosHistorial() {
        Visitante v = new Visitante("TST1", "Test User", "000", 25, 1.70);

        v.registrarVisita("A1");
        v.registrarVisita("A2");
        v.registrarVisita("A1"); // puede repetirse en historial

        assertEquals(3, v.getHistorialVisitas().tamanio());

        v.agregarFavorito("A1");
        v.agregarFavorito("A3");
        v.agregarFavorito("A1"); // duplicado → no debe agregar

        assertEquals(2, v.getFavoritos().tamanio());
        assertTrue(v.esFavorito("A1"));
        assertFalse(v.esFavorito("A9"));

        v.eliminarFavorito("A1");
        assertFalse(v.esFavorito("A1"));
    }
}
