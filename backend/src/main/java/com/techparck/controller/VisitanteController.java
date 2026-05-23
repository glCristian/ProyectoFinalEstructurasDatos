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
    }

    
}


