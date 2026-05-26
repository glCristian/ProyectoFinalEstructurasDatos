package com.techpark;

import com.techpark.model.Atraccion;
import com.techpark.model.EstadoAtraccion;
import com.techpark.model.TipoAtraccion;
import com.techpark.model.TipoTicket;
import com.techpark.model.Ticket;
import com.techpark.model.Visitante;
import com.techpark.model.Zona;
import com.techpark.service.ParqueService;
import com.techpark.structures.ABB;
import com.techpark.structures.ColaPrioridad;
import com.techpark.structures.Grafo;
import com.techpark.structures.ListaEnlazada;
import com.techpark.structures.SetPropio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del proyecto Tech-Park UQ.
 * Cada prueba es independiente y no depende del orden de ejecución.
 */
class TechParkTest {

    @BeforeEach
    void resetParqueService() {
        resetSingleton();
    }

    @Test
    @DisplayName("ListaEnlazada: agregar, buscar, eliminar y mantener orden")
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

    @Test
    @DisplayName("SetPropio: evita duplicados y conserva consistencia")
    void testSetPropio() {
        SetPropio<String> set = new SetPropio<>();

        assertTrue(set.estaVacio());
        assertTrue(set.agregar("A1"));
        assertTrue(set.agregar("A2"));
        assertFalse(set.agregar("A1"));

        assertEquals(2, set.tamanio());
        assertTrue(set.contiene("A1"));
        assertFalse(set.contiene("A9"));

        assertTrue(set.eliminar("A1"));
        assertFalse(set.contiene("A1"));
        assertEquals(1, set.tamanio());
    }

    @Test
    @DisplayName("ColaPrioridad: FastPass sale antes y respeta FIFO")
    void testColaPrioridad() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();

        cola.insertar("General-1", 2);
        cola.insertar("General-2", 2);
        cola.insertar("FastPass-1", 1);
        cola.insertar("General-3", 2);
        cola.insertar("FastPass-2", 1);

        assertEquals(5, cola.tamanio());
        assertEquals("FastPass-1", cola.extraer());
        assertEquals("FastPass-2", cola.extraer());
        assertEquals("General-1", cola.extraer());
        assertEquals("General-2", cola.extraer());
        assertEquals("General-3", cola.extraer());
        assertTrue(cola.estaVacia());
    }

    @Test
    @DisplayName("ABB: insertar, buscar, recorrer y eliminar")
    void testABB() {
        ABB<String, Integer> abb = new ABB<>();

        abb.insertar("b", 2);
        abb.insertar("a", 5);
        abb.insertar("d", 1);
        abb.insertar("c", 4);

        assertEquals(4, abb.tamanio());
        assertEquals(2, abb.buscar("b"));
        assertNull(abb.buscar("z"));
        assertEquals(List.of(5, 2, 4, 1), abb.inorden());

        abb.eliminar("b");
        assertFalse(abb.contiene("b"));
        assertEquals(3, abb.tamanio());
    }

    @Test
    @DisplayName("Grafo: BFS recorre nodos y Dijkstra halla el camino mínimo")
    void testGrafo() {
        Grafo grafo = new Grafo();
        grafo.conectar("A1", "A2", 10);
        grafo.conectar("A2", "A3", 5);
        grafo.conectar("A1", "A3", 20);
        grafo.conectar("A3", "A4", 3);

        List<String> bfs = grafo.bfs("A1");
        assertEquals(4, bfs.size());
        assertTrue(bfs.contains("A4"));

        List<String> camino = grafo.dijkstra("A1", "A4");
        assertFalse(camino.isEmpty());
        assertEquals("A1", camino.get(0));
        assertEquals("A4", camino.get(camino.size() - 1));
        assertEquals(18.0, grafo.distanciaDijkstra("A1", "A4"), 0.001);

        grafo.agregarNodo("AISLADO");
        assertFalse(grafo.estanConectados("A1", "AISLADO"));
    }

    @Test
    @DisplayName("Atraccion: activa mantenimiento al llegar a 500 visitantes")
    void testMantenimientoPreventivo() {
        Atraccion atraccion = new Atraccion("T1", "Test", TipoAtraccion.MECANICA_SUELO, 10, 1.0, 5, 0, "Z1");

        assertEquals(EstadoAtraccion.ACTIVA, atraccion.getEstado());

        for (int i = 0; i < 499; i++) {
            atraccion.registrarVisitante();
        }

        assertEquals(EstadoAtraccion.ACTIVA, atraccion.getEstado());

        boolean activoMantenimiento = atraccion.registrarVisitante();
        assertTrue(activoMantenimiento);
        assertEquals(EstadoAtraccion.EN_MANTENIMIENTO, atraccion.getEstado());

        atraccion.registrarRevisionTecnica();
        assertEquals(EstadoAtraccion.ACTIVA, atraccion.getEstado());
        assertEquals(0, atraccion.getVisitantesAcumulados());
    }

    @Test
    @DisplayName("Visitante: historial y favoritos se comportan de forma independiente")
    void testVisitanteFavoritosHistorial() {
        Visitante visitante = new Visitante("TST1", "Test User", "000", 25, 1.70);

        visitante.registrarVisita("A1");
        visitante.registrarVisita("A2");
        visitante.registrarVisita("A1");

        assertEquals(3, visitante.getHistorialVisitas().tamanio());
        assertTrue(visitante.agregarFavorito("A1"));
        assertTrue(visitante.agregarFavorito("A3"));
        assertFalse(visitante.agregarFavorito("A1"));
        assertEquals(2, visitante.getFavoritos().tamanio());
        assertTrue(visitante.esFavorito("A1"));

        assertTrue(visitante.eliminarFavorito("A1"));
        assertFalse(visitante.esFavorito("A1"));
    }

    @Test
    @DisplayName("ParqueService: compra ticket general, bloquea duplicados y permite cancelar")
    void testCompraGeneralYCancelacion() {
        ParqueService servicio = nuevoServicio();
        Visitante visitante = new Visitante("V1", "Usuario Uno", "111", 20, 1.70);
        visitante.setSaldoVirtual(100000);

        assertTrue(servicio.registrarVisitante(visitante));

        Ticket ticket = servicio.comprarTicketGeneral("V1");
        assertNotNull(ticket);
        assertEquals(TipoTicket.GENERAL, ticket.getTipo());
        assertTrue(ticket.isActivo());
        assertTrue(servicio.getVisitante("V1").isPuedeDentrar());
        assertEquals(60000.0, servicio.getVisitante("V1").getSaldoVirtual(), 0.001);

        Ticket segundoIntento = servicio.comprarTicketGeneral("V1");
        assertNull(segundoIntento);

        assertTrue(servicio.cancelarTicket("V1"));
        assertFalse(servicio.getVisitante("V1").isPuedeDentrar());
        assertFalse(servicio.getVisitante("V1").getTicketComprado().isActivo());

        Ticket recompra = servicio.comprarTicketGeneral("V1");
        assertNotNull(recompra);
        assertTrue(recompra.isActivo());
        assertTrue(servicio.getVisitante("V1").isPuedeDentrar());
    }

    @Test
    @DisplayName("ParqueService: compra ticket familiar y la cancelación afecta a todo el grupo")
    void testCompraFamiliarYCancelacion() {
        ParqueService servicio = nuevoServicio();

        Visitante lider = new Visitante("L1", "Lider", "201", 30, 1.80);
        Visitante miembro1 = new Visitante("M1", "Miembro 1", "202", 12, 1.40);
        Visitante miembro2 = new Visitante("M2", "Miembro 2", "203", 10, 1.30);
        lider.setSaldoVirtual(200000);

        assertTrue(servicio.registrarVisitante(lider));
        assertTrue(servicio.registrarVisitante(miembro1));
        assertTrue(servicio.registrarVisitante(miembro2));

        Ticket ticket = servicio.comprarTicketFamiliar("L1", 3, List.of("M1", "M2"));
        assertNotNull(ticket);
        assertEquals(TipoTicket.FAMILIAR, ticket.getTipo());
        assertEquals(3, ticket.getNumeroPersonasIncluidas());
        assertEquals(3, ticket.getMiembrosDeLaFamilia().size());
        assertTrue(servicio.getVisitante("L1").isPuedeDentrar());
        assertTrue(servicio.getVisitante("M1").isPuedeDentrar());
        assertTrue(servicio.getVisitante("M2").isPuedeDentrar());

        assertTrue(servicio.cancelarTicket("M1"));
        assertFalse(servicio.getVisitante("L1").isPuedeDentrar());
        assertFalse(servicio.getVisitante("M1").isPuedeDentrar());
        assertFalse(servicio.getVisitante("M2").isPuedeDentrar());
        assertFalse(servicio.getVisitante("L1").getTicketComprado().isActivo());
    }

    @Test
    @DisplayName("ParqueService: alerta climática registra historial y reabre atracciones")
    void testAlertaClimaticaConHistorial() {
        ParqueService servicio = nuevoServicio();

        servicio.agregarZona(new Zona("Z1", "Zona Agua", 100));
        servicio.agregarZona(new Zona("Z2", "Zona Aire", 100));
        servicio.agregarZona(new Zona("Z3", "Zona Shows", 100));

        Atraccion acuatica = new Atraccion("C1", "Laguna", TipoAtraccion.ACUATICA, 20, 1.0, 5, 0, "Z1");
        Atraccion mecanica = new Atraccion("C2", "Caida", TipoAtraccion.MECANICA_ALTURA, 12, 1.4, 14, 0, "Z2");
        Atraccion show = new Atraccion("C3", "Show", TipoAtraccion.SHOW, 200, 0, 0, 0, "Z3");

        assertTrue(servicio.agregarAtraccion(acuatica));
        assertTrue(servicio.agregarAtraccion(mecanica));
        assertTrue(servicio.agregarAtraccion(show));

        List<String> cerradas = servicio.activarAlertaClimatica("TORMENTA_ELECTRICA");
        assertEquals(2, cerradas.size());
        assertTrue(cerradas.contains("C1"));
        assertTrue(cerradas.contains("C2"));
        assertFalse(cerradas.contains("C3"));
        assertEquals(EstadoAtraccion.CERRADA, servicio.getAtraccion("C1").getEstado());
        assertEquals(EstadoAtraccion.CERRADA, servicio.getAtraccion("C2").getEstado());
        assertEquals(EstadoAtraccion.ACTIVA, servicio.getAtraccion("C3").getEstado());

        List<ParqueService.CierreClimaticoRegistro> historial = servicio.getHistorialCierresClima();
        assertEquals(2, historial.size());
        assertAll(
            () -> assertEquals("TORMENTA_ELECTRICA", historial.get(0).getTipoAlerta()),
            () -> assertNotNull(historial.get(0).getFechaCierre()),
            () -> assertNotNull(historial.get(0).getAtraccionNombre()),
            () -> assertNotNull(historial.get(1).getMotivoCierre())
        );

        servicio.desactivarAlertaClimatica();
        assertFalse(servicio.isAlertaClimatica());
        assertEquals(EstadoAtraccion.ACTIVA, servicio.getAtraccion("C1").getEstado());
        assertEquals(EstadoAtraccion.ACTIVA, servicio.getAtraccion("C2").getEstado());
        assertEquals(EstadoAtraccion.ACTIVA, servicio.getAtraccion("C3").getEstado());
    }

    private ParqueService nuevoServicio() {
        resetSingleton();
        return ParqueService.getInstance();
    }

    private void resetSingleton() {
        try {
            Field field = ParqueService.class.getDeclaredField("instancia");
            field.setAccessible(true);
            field.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo reiniciar ParqueService para la prueba", e);
        }
    }
}
