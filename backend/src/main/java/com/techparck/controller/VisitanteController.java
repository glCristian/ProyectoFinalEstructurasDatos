package main.java.com.techparck.controller;

import com.google.gson.Gson;
import com.techpark.model.*;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.Map;

public class VisitanteController {
    private final ParqueService parqueService = ParqueService.getInstance();
    private final Gson gson = new Gson();

    public void registrarRutas(Javalin app) {

        // POST /visitantes → registrar visitante
        app.post("/visitantes", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            try {
                Visitante v = new Visitante(
                    (String) body.get("id"),
                    (String) body.get("nombre"),
                    (String) body.get("documento"),
                    ((Number) body.get("edad")).intValue(),
                    ((Number) body.get("altura")).doubleValue()
                );
                if (body.containsKey("saldoVirtual"))
                    v.setSaldoVirtual(((Number) body.get("saldoVirtual")).doubleValue());
                TipoTicket tipo  = TipoTicket.valueOf(((String) body.get("tipoTicket")).toUpperCase());
                double precio    = body.containsKey("precioTicket")
                                   ? ((Number) body.get("precioTicket")).doubleValue() : 40000;
                boolean ok = parqueService.registrarVisitante(v, tipo, precio);
                ctx.status(ok ? 201 : 409)
                   .result(ok ? "Visitante registrado" : "ID duplicado o parque sin cupo");
            } catch (Exception e) {
                ctx.status(400).result("Datos inválidos: " + e.getMessage());
            }
        });

        // GET /visitantes → listar todos
        app.get("/visitantes", ctx -> {
            ctx.json(gson.toJson(parqueService.getVisitantes()));
        });

        // GET /visitantes/{id} → obtener por ID
        app.get("/visitantes/{id}", ctx -> {
            Visitante v = parqueService.getVisitante(ctx.pathParam("id"));
            if (v == null) { ctx.status(404).result("Visitante no encontrado"); return; }
            ctx.json(gson.toJson(v));
        });

        // DELETE /visitantes/{id} → retirar visitante del parque
        app.delete("/visitantes/{id}", ctx -> {
            boolean ok = parqueService.retirarVisitante(ctx.pathParam("id"));
            ctx.status(ok ? 200 : 404).result(ok ? "Visitante retirado" : "No encontrado");
        });

        // POST /visitantes/{id}/cola/{atraccionId} → ingresar a cola de atracción
        app.post("/visitantes/{id}/cola/{atraccionId}", ctx -> {
            String resultado = parqueService.ingresarACola(
                ctx.pathParam("id"),
                ctx.pathParam("atraccionId")
            );
            if ("OK".equals(resultado)) ctx.result("Ingresado a la cola");
            else ctx.status(400).result(resultado);
        });

        // GET /visitantes/{id}/historial → historial de visitas
        app.get("/visitantes/{id}/historial", ctx -> {
            Visitante v = parqueService.getVisitante(ctx.pathParam("id"));
            if (v == null) { ctx.status(404).result("No encontrado"); return; }
            ctx.json(gson.toJson(v.getHistorialVisitas().aArreglo()));
        });

        // GET /visitantes/{id}/favoritos → favoritos
        app.get("/visitantes/{id}/favoritos", ctx -> {
            Visitante v = parqueService.getVisitante(ctx.pathParam("id"));
            if (v == null) { ctx.status(404).result("No encontrado"); return; }
            ctx.json(gson.toJson(v.getFavoritos()));
        });

        // POST /visitantes/{id}/favoritos/{atraccionId} → agregar favorito
        app.post("/visitantes/{id}/favoritos/{atraccionId}", ctx -> {
            Visitante v = parqueService.getVisitante(ctx.pathParam("id"));
            if (v == null) { ctx.status(404).result("No encontrado"); return; }
            boolean ok = v.agregarFavorito(ctx.pathParam("atraccionId"));
            ctx.result(ok ? "Favorito agregado" : "Ya existe en favoritos");
        });

        // DELETE /visitantes/{id}/favoritos/{atraccionId} → quitar favorito
        app.delete("/visitantes/{id}/favoritos/{atraccionId}", ctx -> {
            Visitante v = parqueService.getVisitante(ctx.pathParam("id"));
            if (v == null) { ctx.status(404).result("No encontrado"); return; }
            boolean ok = v.eliminarFavorito(ctx.pathParam("atraccionId"));
            ctx.status(ok ? 200 : 404).result(ok ? "Favorito eliminado" : "No encontrado en favoritos");
        });

        // POST /visitantes/{id}/saldo → recargar saldo
        app.post("/visitantes/{id}/saldo", ctx -> {
            Visitante v = parqueService.getVisitante(ctx.pathParam("id"));
            if (v == null) { ctx.status(404).result("No encontrado"); return; }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            double monto = ((Number) body.get("monto")).doubleValue();
            v.recargarSaldo(monto);
            ctx.json(gson.toJson(Map.of("saldoActual", v.getSaldoVirtual())));
        });
    }

    
}


