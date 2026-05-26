package main.java.com.techparck.controller;

import com.google.gson.Gson;
import com.techpark.model.Atraccion;
import com.techpark.model.EstadoAtraccion;
import com.techpark.model.TipoAtraccion;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.Map;

public class AtraccionController {
    private final ParqueService parqueService = ParqueService.getInstance();
    private final Gson gson = new Gson();

    public void registrarRutas(Javalin app) {

        // GET /atracciones → listar todas
        app.get("/atracciones", ctx -> {
            ctx.json(gson.toJson(parqueService.getAtracciones()));
        });

        // GET /atracciones/{id} → obtener por ID
        app.get("/atracciones/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Atraccion a = parqueService.getAtraccion(id);
            if (a == null) { ctx.status(404).result("Atracción no encontrada"); return; }
            ctx.json(gson.toJson(a));
        });

        // GET /atracciones/buscar/{nombre} → buscar por nombre (ABB)
        app.get("/atracciones/buscar/{nombre}", ctx -> {
            String nombre = ctx.pathParam("nombre");
            Atraccion a = parqueService.buscarAtraccionPorNombre(nombre);
            if (a == null) { ctx.status(404).result("No encontrada"); return; }
            ctx.json(gson.toJson(a));
        });

        // POST /atracciones → crear atracción
        app.post("/atracciones", ctx -> {
            Atraccion a = gson.fromJson(ctx.body(), Atraccion.class);
            if (a == null || a.getId() == null) { ctx.status(400).result("Datos inválidos"); return; }
            boolean ok = parqueService.agregarAtraccion(a);
            ctx.status(ok ? 201 : 409).result(ok ? "Atracción creada" : "ID duplicado");
        });

        // PUT /atracciones/{id}/estado → cambiar estado
        app.put("/atracciones/{id}/estado", ctx -> {
            String id = ctx.pathParam("id");
            @SuppressWarnings("unchecked")
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            try {
                EstadoAtraccion estado = EstadoAtraccion.valueOf(body.get("estado").toUpperCase());
                String motivo = body.getOrDefault("motivo", "");
                boolean ok = parqueService.actualizarEstadoAtraccion(id, estado, motivo);
                ctx.status(ok ? 200 : 404).result(ok ? "Estado actualizado" : "Atracción no encontrada");
            } catch (Exception e) {
                ctx.status(400).result("Estado inválido: " + e.getMessage());
            }
        });

        // PUT /atracciones/{id}/revision → registrar revisión técnica
        app.put("/atracciones/{id}/revision", ctx -> {
            String id = ctx.pathParam("id");
            Atraccion a = parqueService.getAtraccion(id);
            if (a == null) { ctx.status(404).result("No encontrada"); return; }
            a.registrarRevisionTecnica();
            ctx.result("Revisión técnica registrada. Atracción reactivada.");
        });

        // DELETE /atracciones/{id} → eliminar
        app.delete("/atracciones/{id}", ctx -> {
            boolean ok = parqueService.eliminarAtraccion(ctx.pathParam("id"));
            ctx.status(ok ? 200 : 404).result(ok ? "Atracción eliminada" : "No encontrada");
        });

        // GET /atracciones/{id}/cola/tamanio → tamaño de la cola
        app.get("/atracciones/{id}/cola/tamanio", ctx -> {
            int t = parqueService.tamanioCola(ctx.pathParam("id"));
            ctx.json(gson.toJson(Map.of("atraccionId", ctx.pathParam("id"), "tamanioCola", t)));
        });
    }    
}
