package main.java.com.techparck.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SetPropio<T> implements Iterable<T> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA   = 0.75;

    // ── Nodo de la tabla hash ─────────────────────────────────────────
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato      = dato;
            this.siguiente = null;
        }
    }

    // ── Atributos ─────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private Nodo<T>[] tabla = new Nodo[CAPACIDAD_INICIAL];
    private int tamanio  = 0;
    private int capacidad = CAPACIDAD_INICIAL;

    // ── Operaciones ───────────────────────────────────────────────────

    /** Agrega un elemento al set. Retorna false si ya existe. */
    public boolean agregar(T dato) {
        if (dato == null) return false;
        if (contiene(dato)) return false;
        if ((double) tamanio / capacidad >= FACTOR_CARGA) redimensionar();

        int indice = indiceHash(dato);
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = tabla[indice];
        tabla[indice]   = nuevo;
        tamanio++;
        return true;
    }

    /** Elimina un elemento del set. Retorna true si lo encontró y eliminó. */
    public boolean eliminar(T dato) {
        if (dato == null) return false;
        int indice = indiceHash(dato);
        Nodo<T> actual = tabla[indice];
        Nodo<T> anterior = null;

        while (actual != null) {
            if (actual.dato.equals(dato)) {
                if (anterior == null) tabla[indice] = actual.siguiente;
                else anterior.siguiente = actual.siguiente;
                tamanio--;
                return true;
            }
            anterior = actual;
            actual   = actual.siguiente;
        }
        return false;
    }

    /** Verifica si el dato está en el set. */
    public boolean contiene(T dato) {
        if (dato == null) return false;
        int indice = indiceHash(dato);
        Nodo<T> actual = tabla[indice];
        while (actual != null) {
            if (actual.dato.equals(dato)) return true;
            actual = actual.siguiente;
        }
        return false;
    }

    /** Retorna el número de elementos en el set. */
    public int tamanio() { return tamanio; }

    /** Verifica si el set está vacío. */
    public boolean estaVacio() { return tamanio == 0; }

    /** Vacía el set. */
    @SuppressWarnings("unchecked")
    public void vaciar() {
        tabla    = new Nodo[CAPACIDAD_INICIAL];
        tamanio  = 0;
        capacidad = CAPACIDAD_INICIAL;
    }

    // ── Auxiliares ────────────────────────────────────────────────────

    private int indiceHash(T dato) {
        return Math.abs(dato.hashCode()) % capacidad;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        capacidad *= 2;
        Nodo<T>[] nuevaTabla = new Nodo[capacidad];
        for (int i = 0; i < tabla.length; i++) {
            Nodo<T> actual = tabla[i];
            while (actual != null) {
                Nodo<T> siguiente = actual.siguiente;
                int nuevo = Math.abs(actual.dato.hashCode()) % capacidad;
                actual.siguiente = nuevaTabla[nuevo];
                nuevaTabla[nuevo] = actual;
                actual = siguiente;
            }
        }
        tabla = nuevaTabla;
    }

    // ── Iterable ──────────────────────────────────────────────────────
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int bucket  = 0;
            Nodo<T> actual = null;

            { avanzar(); }

            private void avanzar() {
                while (bucket < capacidad && actual == null) {
                    actual = tabla[bucket++];
                }
            }

            @Override public boolean hasNext() { return actual != null; }

            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T dato = actual.dato;
                actual = actual.siguiente;
                if (actual == null) avanzar();
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean primero = true;
        for (T item : this) {
            if (!primero) sb.append(", ");
            sb.append(item);
            primero = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
