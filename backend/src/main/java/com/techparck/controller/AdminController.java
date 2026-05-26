package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.util.JsonConfig;
import com.techpark.service.CargaDatosService;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.Map;

public class AdminController {

    private final ParqueService    parqueService    = ParqueService.getInstance();
    private final CargaDatosService cargaDatosService = new CargaDatosService(parqueService);
    private static final Gson gson = JsonConfig.GSON;

    public void registrarRutas(Javalin app) {

        // GET /admin/reportes/top-atracciones?n=5
        app.get("/admin/reportes/top-atracciones", ctx -> {
            int n = Integer.parseInt(ctx.queryParamAsClass("n", String.class).getOrDefault("5"));
            ctx.json(gson.toJson(parqueService.topAtracciones(n)));
        });

        // GET /admin/reportes/mantenimiento
        app.get("/admin/reportes/mantenimiento", ctx ->
            ctx.json(gson.toJson(parqueService.atraccionesEnMantenimiento())));

        // GET /admin/reportes/cierres-clima
        app.get("/admin/reportes/cierres-clima", ctx ->
            ctx.json(gson.toJson(parqueService.getHistorialCierresClima())));

        // GET /admin/reportes/ingreso-diario
        app.get("/admin/reportes/ingreso-diario", ctx ->
            ctx.json(gson.toJson(Map.of("ingresoDiario", parqueService.calcularIngresoDiario()))));

        // GET /admin/reportes/tiempo-espera-promedio
        app.get("/admin/reportes/tiempo-espera-promedio", ctx ->
            ctx.json(gson.toJson(Map.of("tiempoPromedioEspera", parqueService.tiempoPromedioEspera()))));

        // GET /admin/reportes/aforo
        app.get("/admin/reportes/aforo", ctx ->
            ctx.json(gson.toJson(Map.of(
                "capacidadMaxima", parqueService.getCapacidadMaximaParque(),
                "visitantesActuales", parqueService.getVisitantesActualesParque()
            ))));

        // POST /admin/datos/prueba → cargar escenario de prueba
        app.post("/admin/datos/prueba", ctx -> {
            cargaDatosService.cargarEscenarioPrueba();
            ctx.result("Escenario de prueba cargado");
        });

        // POST /admin/datos/archivo → cargar desde archivo
        app.post("/admin/datos/archivo", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            String ruta = body.get("ruta");
            if (ruta == null || ruta.isBlank()) { ctx.status(400).result("Ruta de archivo requerida"); return; }
            int count = cargaDatosService.cargarDesdeArchivo(ruta);
            ctx.json(gson.toJson(Map.of("entidadesCargadas", count)));
        });

        // PUT /admin/parque/capacidad → ajustar capacidad máxima del parque
        app.put("/admin/parque/capacidad", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            int cap = ((Number) body.get("capacidad")).intValue();
            parqueService.setCapacidadMaximaParque(cap);
            ctx.result("Capacidad actualizada a " + cap);
        });
    }
}
