package main.java.com.techparck.controller;

import com.google.gson.Gson;
import com.techpark.service.ParqueService;
import com.techpark.service.RutaService;
import io.javalin.Javalin;

import java.util.Map;

public class RutaController {
    private final RutaService rutaService = new RutaService(ParqueService.getInstance());
    private final Gson gson = new Gson();

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
            ctx.result("Conexión creada entre " + idA + " y " + idB);
        });
    }
}
