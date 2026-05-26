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

}
