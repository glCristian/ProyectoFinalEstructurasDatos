package com.techpark.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Cola de prioridad implementada manualmente con min-heap.
 * Gestiona la fila de ingreso a las atracciones:
 *   Prioridad 1 → FastPass  (se atiende primero)
 *   Prioridad 2 → General / Familiar
 *
 * @param <T> tipo de dato almacenado.
 */
public class ColaPrioridad<T> {

    // ── Nodo con prioridad ────────────────────────────────────────────
    public static class Elemento<T> {
        public final T   dato;
        public final int prioridad;     // menor valor = mayor prioridad
        public final long orden;        // desempate FIFO

        public Elemento(T dato, int prioridad, long orden) {
            this.dato      = dato;
            this.prioridad = prioridad;
            this.orden     = orden;
        }
    }

    // ── Atributos ─────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private Elemento<T>[] heap = new Elemento[16];
    private int tamanio        = 0;
    private long contadorOrden = 0L;   // garantiza FIFO dentro del mismo nivel

    // ── Operaciones ───────────────────────────────────────────────────

    /** Inserta un elemento con la prioridad indicada (1=mayor). */
    public void insertar(T dato, int prioridad) {
        asegurarCapacidad();
        heap[tamanio] = new Elemento<>(dato, prioridad, contadorOrden++);
        subirBurbuja(tamanio);
        tamanio++;
    }

    /** Extrae y retorna el elemento de mayor prioridad. */
    public T extraer() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía");
        T resultado = heap[0].dato;
        heap[0] = heap[--tamanio];
        heap[tamanio] = null;
        if (!estaVacia()) bajarBurbuja(0);
        return resultado;
    }

    /** Consulta el elemento de mayor prioridad sin extraerlo. */
    public T peek() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía");
        return heap[0].dato;
    }

    /** Consulta el elemento completo (con prioridad) al tope. */
    public Elemento<T> peekElemento() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía");
        return heap[0];
    }

    /** Retorna el número de elementos en la cola. */
    public int tamanio() { return tamanio; }

    /** Verifica si la cola está vacía. */
    public boolean estaVacia() { return tamanio == 0; }

    /** Vacía la cola. */
    @SuppressWarnings("unchecked")
    public void vaciar() {
        heap    = new Elemento[16];
        tamanio = 0;
        contadorOrden = 0L;
    }

    /** Retorna una lista con los elementos actuales (sin alterar el heap). */
    public List<T> toList() {
        List<T> lista = new ArrayList<>(tamanio);
        for (int i = 0; i < tamanio; i++) {
            if (heap[i] != null) lista.add(heap[i].dato);
        }
        return lista;
    }

    /**
     * Retorna una lista ordenada por prioridad (sin alterar el heap).
     * El orden respeta la prioridad y el FIFO interno de la cola.
     */
    public List<T> toOrderedList() {
        List<T> ordenada = new ArrayList<>(tamanio);
        if (tamanio == 0) return ordenada;

        @SuppressWarnings("unchecked")
        Elemento<T>[] copia = new Elemento[tamanio];
        System.arraycopy(heap, 0, copia, 0, tamanio);

        int size = tamanio;
        while (size > 0) {
            Elemento<T> top = copia[0];
            ordenada.add(top.dato);
            copia[0] = copia[--size];
            copia[size] = null;
            if (size > 0) bajarBurbujaCopia(copia, size, 0);
        }

        return ordenada;
    }

    // ── Auxiliares del heap ───────────────────────────────────────────

    private void subirBurbuja(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (comparar(heap[i], heap[padre]) < 0) {
                intercambiar(i, padre);
                i = padre;
            } else break;
        }
    }

    private void bajarBurbuja(int i) {
        while (true) {
            int izq   = 2 * i + 1;
            int der   = 2 * i + 2;
            int menor = i;
            if (izq < tamanio && comparar(heap[izq], heap[menor]) < 0) menor = izq;
            if (der < tamanio && comparar(heap[der], heap[menor]) < 0) menor = der;
            if (menor != i) { intercambiar(i, menor); i = menor; }
            else break;
        }
    }

    private void bajarBurbujaCopia(Elemento<T>[] arr, int size, int i) {
        while (true) {
            int izq   = 2 * i + 1;
            int der   = 2 * i + 2;
            int menor = i;
            if (izq < size && comparar(arr[izq], arr[menor]) < 0) menor = izq;
            if (der < size && comparar(arr[der], arr[menor]) < 0) menor = der;
            if (menor != i) {
                Elemento<T> tmp = arr[i];
                arr[i] = arr[menor];
                arr[menor] = tmp;
                i = menor;
            } else break;
        }
    }

    /** Compara por prioridad; desempata por orden de inserción (FIFO). */
    private int comparar(Elemento<T> a, Elemento<T> b) {
        if (a.prioridad != b.prioridad) return Integer.compare(a.prioridad, b.prioridad);
        return Long.compare(a.orden, b.orden);
    }

    private void intercambiar(int i, int j) {
        Elemento<T> tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    @SuppressWarnings("unchecked")
    private void asegurarCapacidad() {
        if (tamanio == heap.length) {
            Elemento<T>[] nuevo = new Elemento[heap.length * 2];
            System.arraycopy(heap, 0, nuevo, 0, tamanio);
            heap = nuevo;
        }
    }
}
