package main.java.com.techparck.structures;

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
