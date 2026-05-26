package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.util.JsonConfig;
import com.techpark.service.ParqueService;
import com.techpark.service.PersistenciaService;
import com.techpark.service.RutaService;
import io.javalin.Javalin;

import java.util.Map;

public class RutaController {

    private final RutaService rutaService = new RutaService(ParqueService.getInstance());
    private final PersistenciaService persistenciaService = new PersistenciaService();
    private static final Gson gson = JsonConfig.GSON;

    public void registrarRutas(Javalin app) {

        // GET /rutas/dijkstra?origen=A1&destino=A5
        app.get("/rutas/dijkstra", ctx -> {
            String origen  = ctx.queryParam("origen");
            String destino = ctx.queryParam("destino");
            if (origen == null || destino == null) {
                ctx.status(400).result("Se requieren parámetros 'origen' y 'destino'");
                return;
            }
            ctx.json(gson.toJson(Map.of(
                "camino",    rutaService.rutaOptima(origen, destino),
                "distancia", rutaService.distanciaRutaOptima(origen, destino)
            )));
        });

        // GET /rutas/bfs?origen=A1
        app.get("/rutas/bfs", ctx -> {
            String origen = ctx.queryParam("origen");
            if (origen == null) { ctx.status(400).result("Se requiere parámetro 'origen'"); return; }
            ctx.json(gson.toJson(rutaService.recorridoBFS(origen)));
        });

        // GET /rutas/grafo → representación completa del grafo para la GUI
        app.get("/rutas/grafo", ctx ->
            ctx.json(gson.toJson(rutaService.obtenerGrafoParaVista())));

        // GET /rutas/clusters → componentes conectados
        app.get("/rutas/clusters", ctx ->
            ctx.json(gson.toJson(rutaService.detectarClusters())));

        // GET /rutas/conectados?a=A1&b=A5
        app.get("/rutas/conectados", ctx -> {
            String a = ctx.queryParam("a");
            String b = ctx.queryParam("b");
            if (a == null || b == null) { ctx.status(400).result("Se requieren 'a' y 'b'"); return; }
            boolean conectados = rutaService.estanConectadas(a, b);
            ctx.json(gson.toJson(Map.of("conectados", conectados)));
        });

        // POST /rutas/conexion → conectar dos atracciones en el grafo
        // Body: { "idA": "A1", "idB": "A3", "peso": 75.0 }
        app.post("/rutas/conexion", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            String idA = (String) body.get("idA");
            String idB = (String) body.get("idB");
            double peso = ((Number) body.get("peso")).doubleValue();
            ParqueService.getInstance().conectarAtracciones(idA, idB, peso);
            persistenciaService.guardarTodo(ParqueService.getInstance());
            ctx.result("Conexión creada entre " + idA + " y " + idB);
        });

        // PUT /rutas/conexion → actualizar peso de una conexión existente
        app.put("/rutas/conexion", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            String idA = (String) body.get("idA");
            String idB = (String) body.get("idB");
            double peso = ((Number) body.get("peso")).doubleValue();
            boolean ok = ParqueService.getInstance().actualizarConexion(idA, idB, peso);
            if (ok) persistenciaService.guardarTodo(ParqueService.getInstance());
            ctx.status(ok ? 200 : 404).result(ok ? "Conexión actualizada" : "Conexión no encontrada");
        });

        // DELETE /rutas/conexion?idA=A1&idB=A3 → eliminar conexión
        app.delete("/rutas/conexion", ctx -> {
            String idA = ctx.queryParam("idA");
            String idB = ctx.queryParam("idB");
            if (idA == null || idB == null) { ctx.status(400).result("Se requieren idA e idB"); return; }
            boolean ok = ParqueService.getInstance().desconectarAtracciones(idA, idB);
            if (ok) persistenciaService.guardarTodo(ParqueService.getInstance());
            ctx.status(ok ? 200 : 404).result(ok ? "Conexión eliminada" : "Conexión no encontrada");
        });

        // PUT /rutas/posicion → guardar posición de una atracción en el mapa
        app.put("/rutas/posicion", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            String id = body != null ? (String) body.get("id") : null;
            double x = body != null && body.get("x") instanceof Number ? ((Number) body.get("x")).doubleValue() : Double.NaN;
            double y = body != null && body.get("y") instanceof Number ? ((Number) body.get("y")).doubleValue() : Double.NaN;
            if (id == null || id.isBlank() || Double.isNaN(x) || Double.isNaN(y)) {
                ctx.status(400).result("id, x e y son requeridos");
                return;
            }

            boolean ok = ParqueService.getInstance().actualizarPosicionAtraccion(id, x, y);
            if (ok) persistenciaService.guardarTodo(ParqueService.getInstance());
            ctx.status(ok ? 200 : 404).result(ok ? "Posición guardada" : "Atracción no encontrada");
        });

        // POST /rutas/posiciones/restaurar → volver al layout base
        app.post("/rutas/posiciones/restaurar", ctx -> {
            ParqueService.getInstance().restaurarPosicionesMapaPorDefecto();
            persistenciaService.guardarTodo(ParqueService.getInstance());
            ctx.result("Posiciones restauradas");
        });
    }
}
