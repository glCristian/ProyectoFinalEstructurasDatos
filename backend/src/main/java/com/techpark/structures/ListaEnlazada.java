package com.techpark.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lista enlazada simple genérica implementada manualmente.
 * Usada para:
 *  - Historial de visitas del visitante.
 *  - Lista de operadores asignados a una zona.
 *  - Lista de IDs de atracciones por zona.
 *
 * @param <T> tipo de dato almacenado.
 */
public class ListaEnlazada<T> implements Iterable<T> {

    // ── Nodo interno ──────────────────────────────────────────────────
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato      = dato;
            this.siguiente = null;
        }
    }

    // ── Atributos ─────────────────────────────────────────────────────
    private Nodo<T> cabeza;
    private int     tamanio;

    // ── Constructores ──────────────────────────────────────────────────
    public ListaEnlazada() {
        this.cabeza  = null;
        this.tamanio = 0;
    }

    // ── Operaciones ───────────────────────────────────────────────────

    /**
     * Convierte la lista a un List de Java. Útil para interoperabilidad.
     */
    public List<T> toList() {
        List<T> result = new ArrayList<>();
        for (T item : this)
            result.add(item);
        return result;
    }


    /** Agrega un elemento al final de la lista. */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) actual = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamanio++;
    }

    /** Agrega un elemento al inicio de la lista. */
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        tamanio++;
    }

    /** Elimina la primera ocurrencia del dato. Retorna true si lo encontró. */
    public boolean eliminar(T dato) {
        if (cabeza == null) return false;
        if (cabeza.dato.equals(dato)) {
            cabeza = cabeza.siguiente;
            tamanio--;
            return true;
        }
        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.equals(dato)) {
                actual.siguiente = actual.siguiente.siguiente;
                tamanio--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    /** Verifica si el dato está en la lista. */
    public boolean contiene(T dato) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.dato.equals(dato)) return true;
            actual = actual.siguiente;
        }
        return false;
    }

    /** Retorna el elemento en la posición indicada (0-indexed). */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) throw new IndexOutOfBoundsException("Índice: " + indice);
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) actual = actual.siguiente;
        return actual.dato;
    }

    /** Retorna el número de elementos. */
    public int tamanio() { return tamanio; }

    /** Verifica si la lista está vacía. */
    public boolean estaVacia() { return tamanio == 0; }

    /** Vacía la lista. */
    public void vaciar() {
        cabeza  = null;
        tamanio = 0;
    }

    /** Convierte la lista a un arreglo de Object. */
    public Object[] aArreglo() {
        Object[] arr = new Object[tamanio];
        Nodo<T> actual = cabeza;
        for (int i = 0; i < tamanio; i++) {
            arr[i]  = actual.dato;
            actual  = actual.siguiente;
        }
        return arr;
    }

    // ── Iterable ──────────────────────────────────────────────────────
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Nodo<T> actual = cabeza;

            @Override public boolean hasNext() { return actual != null; }

            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" -> ");
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}
