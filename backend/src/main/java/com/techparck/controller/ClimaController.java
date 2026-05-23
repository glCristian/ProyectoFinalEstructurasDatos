package main.java.com.techparck.controller;

import com.google.gson.Gson;
import main.java.com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.List;
import java.util.Map;

public class ClimaController {

    private final ParqueService parqueService = ParqueService.getInstance();
    private final Gson gson = new Gson();

    public void registrarRutas(Javalin app) {

        // GET /clima → estado climático actual
        app.get("/clima", ctx ->
            ctx.json(gson.toJson(Map.of(
                "alertaActiva", parqueService.isAlertaClimatica(),
                "tipoAlerta",   parqueService.getTipoAlerta()
            ))));
        // DELETE /clima/alerta → desactivar alerta climática
        app.delete("/clima/alerta", ctx -> {
            parqueService.desactivarAlertaClimatica();
            ctx.result("Alerta climática desactivada");
        });
    }
}


