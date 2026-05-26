# Tech-Park UQ - Pruebas Unitarias

La suite principal de pruebas del backend vive en `src/test/java/com/techpark/TechParkTest.java`.

## Qué valida

La suite cubre estructuras de datos propias y lógica de negocio del parque:

1. `ListaEnlazada`
   - Agregar elementos.
   - Buscar y eliminar.
   - Mantener el orden de inserción.

2. `SetPropio`
   - Evitar duplicados.
   - Agregar, consultar y eliminar elementos.

3. `ColaPrioridad`
   - Prioridad FastPass antes que General.
   - Orden FIFO entre elementos con la misma prioridad.

4. `ABB`
   - Insertar claves.
   - Buscar valores.
   - Recorrer en orden.
   - Eliminar nodos.

5. `Grafo`
   - Recorrido BFS.
   - Ruta mínima con Dijkstra.
   - Detección de nodos desconectados.

6. `Atraccion`
   - Activación de mantenimiento preventivo al llegar a 500 visitantes.
   - Reinicio del estado con revisión técnica.

7. `Visitante`
   - Registro del historial de visitas.
   - Manejo independiente de favoritos.

8. `ParqueService`
   - Compra de ticket general.
   - Bloqueo de compras duplicadas.
   - Cancelación de ticket.

9. `ParqueService`
   - Compra de ticket familiar.
   - Cancelación del grupo completo.

10. `ParqueService`
    - Activación de alerta climática.
    - Registro histórico de cierres.
    - Reapertura de atracciones al desactivar la alerta.

## Cómo ejecutarlas

```bash
cd backend
mvn test
```

Si solo quieres validar esta clase:

```bash
cd backend
mvn -Dtest=TechParkTest test
```

## Observaciones

- Las pruebas se ejecutan de forma independiente.
- `TechParkTest` reinicia el singleton de `ParqueService` antes de cada prueba para evitar contaminación de estado.
- El objetivo es validar la lógica interna y no la interfaz web ni la persistencia completa.
