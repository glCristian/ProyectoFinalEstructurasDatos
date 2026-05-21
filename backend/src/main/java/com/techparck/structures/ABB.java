package main.java.com.techparck.structures;

import java.util.ArrayList;
import java.util.List;

public class ABB<K extends Comparable<K>, V> {

    // ── Nodo interno ──────────────────────────────────────────────────
    private static class Nodo<K, V> {
        K clave;
        V valor;
        Nodo<K, V> izquierdo;
        Nodo<K, V> derecho;

        Nodo(K clave, V valor) {
            this.clave     = clave;
            this.valor     = valor;
            this.izquierdo = null;
            this.derecho   = null;
        }
    }

    // ── Atributos ─────────────────────────────────────────────────────
    private Nodo<K, V> raiz;
    private int tamanio = 0;

    // ── Operaciones ───────────────────────────────────────────────────

    /** Inserta una clave-valor. Si la clave existe, actualiza el valor. */
    public void insertar(K clave, V valor) {
        raiz = insertarRec(raiz, clave, valor);
    }

    private Nodo<K, V> insertarRec(Nodo<K, V> nodo, K clave, V valor) {
        if (nodo == null) { tamanio++; return new Nodo<>(clave, valor); }
        int cmp = clave.compareTo(nodo.clave);
        if      (cmp < 0) nodo.izquierdo = insertarRec(nodo.izquierdo, clave, valor);
        else if (cmp > 0) nodo.derecho   = insertarRec(nodo.derecho,   clave, valor);
        else               nodo.valor    = valor; // actualizar
        return nodo;
    }

    /** Busca un valor por clave. Retorna null si no existe. */
    public V buscar(K clave) {
        Nodo<K, V> nodo = buscarNodo(raiz, clave);
        return nodo != null ? nodo.valor : null;
    }

    private Nodo<K, V> buscarNodo(Nodo<K, V> nodo, K clave) {
        if (nodo == null) return null;
        int cmp = clave.compareTo(nodo.clave);
        if      (cmp < 0) return buscarNodo(nodo.izquierdo, clave);
        else if (cmp > 0) return buscarNodo(nodo.derecho,   clave);
        else               return nodo;
    }

    /** Elimina un nodo por clave. */
    public void eliminar(K clave) {
        raiz = eliminarRec(raiz, clave);
    }

    private Nodo<K, V> eliminarRec(Nodo<K, V> nodo, K clave) {
        if (nodo == null) return null;
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) {
            nodo.izquierdo = eliminarRec(nodo.izquierdo, clave);
        } else if (cmp > 0) {
            nodo.derecho = eliminarRec(nodo.derecho, clave);
        } else {
            tamanio--;
            if (nodo.izquierdo == null) return nodo.derecho;
            if (nodo.derecho   == null) return nodo.izquierdo;
            // Sucesor inorden (mínimo del subárbol derecho)
            Nodo<K, V> sucesor = minimo(nodo.derecho);
            nodo.clave  = sucesor.clave;
            nodo.valor  = sucesor.valor;
            nodo.derecho = eliminarRec(nodo.derecho, sucesor.clave);
        }
        return nodo;
    }

    private Nodo<K, V> minimo(Nodo<K, V> nodo) {
        while (nodo.izquierdo != null) nodo = nodo.izquierdo;
        return nodo;
    }

    /** Recorrido inorden → lista de valores en orden ascendente de clave. */
    public List<V> inorden() {
        List<V> lista = new ArrayList<>();
        inordenRec(raiz, lista);
        return lista;
    }

    private void inordenRec(Nodo<K, V> nodo, List<V> lista) {
        if (nodo == null) return;
        inordenRec(nodo.izquierdo, lista);
        lista.add(nodo.valor);
        inordenRec(nodo.derecho, lista);
    }

    /** Verifica si una clave existe en el árbol. */
    public boolean contiene(K clave) { return buscar(clave) != null; }

    /** Retorna el número de nodos. */
    public int tamanio() { return tamanio; }

    /** Verifica si el árbol está vacío. */
    public boolean estaVacio() { return raiz == null; }
}
