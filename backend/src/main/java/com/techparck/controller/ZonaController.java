package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.util.JsonConfig;
import com.techpark.model.Zona;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.Map;

public class ZonaController {

    private final ParqueService parqueService = ParqueService.getInstance();
    private static final Gson gson = JsonConfig.GSON;

    public void registrarRutas(Javalin app) {

        // GET /zonas
        app.get("/zonas", ctx ->
            ctx.json(gson.toJson(parqueService.getZonas())));

        // GET /zonas/{id}
        app.get("/zonas/{id}", ctx -> {
            Zona z = parqueService.getZona(ctx.pathParam("id"));
            if (z == null) { ctx.status(404).result("Zona no encontrada"); return; }
            ctx.json(gson.toJson(z));
        });

        // POST /zonas → crear zona
        app.post("/zonas", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            Zona z = new Zona(
                (String) body.get("id"),
                (String) body.get("nombre"),
                ((Number) body.get("capacidadMaxima")).intValue()
            );
            boolean ok = parqueService.agregarZona(z);
            ctx.status(ok ? 201 : 409).result(ok ? "Zona creada" : "ID duplicado");
        });

        // PUT /zonas/{id} → actualizar zona
        app.put("/zonas/{id}", ctx -> {
            Zona z = parqueService.getZona(ctx.pathParam("id"));
            if (z == null) { ctx.status(404).result("Zona no encontrada"); return; }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);

            if (body.containsKey("nombre")) {
                String nombre = (String) body.get("nombre");
                if (nombre != null && !nombre.isBlank()) z.setNombre(nombre);
            }
            if (body.containsKey("capacidadMaxima")) {
                z.setCapacidadMaxima(((Number) body.get("capacidadMaxima")).intValue());
            }

            ctx.result("Zona actualizada");
        });

        // DELETE /zonas/{id}
        app.delete("/zonas/{id}", ctx -> {
            boolean ok = parqueService.eliminarZona(ctx.pathParam("id"));
            ctx.status(ok ? 200 : 404).result(ok ? "Zona eliminada" : "No encontrada");
        });
    }
}
