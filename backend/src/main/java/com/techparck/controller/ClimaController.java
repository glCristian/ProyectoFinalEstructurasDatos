package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.util.JsonConfig;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.List;
import java.util.Map;

public class ClimaController {

    private final ParqueService parqueService = ParqueService.getInstance();
    private static final Gson gson = JsonConfig.GSON;

    public void registrarRutas(Javalin app) {

        // GET /clima → estado climático actual
        app.get("/clima", ctx ->
            ctx.json(gson.toJson(Map.of(
                "alertaActiva", parqueService.isAlertaClimatica(),
                "tipoAlerta",   parqueService.getTipoAlerta()
            ))));

        // POST /clima/alerta → activar alerta climática
        // Body: { "tipo": "TORMENTA_ELECTRICA" | "LLUVIA_FUERTE" }
        app.post("/clima/alerta", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            String tipo = body.getOrDefault("tipo", "ALERTA_GENERAL");
            List<String> cerradas = parqueService.activarAlertaClimatica(tipo);
            ctx.json(gson.toJson(Map.of(
                "mensaje",   "Alerta climática activada: " + tipo,
                "cerradas",  cerradas
            )));
        });

        // DELETE /clima/alerta → desactivar alerta climática
        app.delete("/clima/alerta", ctx -> {
            parqueService.desactivarAlertaClimatica();
            ctx.result("Alerta climática desactivada");
        });
    }
}
