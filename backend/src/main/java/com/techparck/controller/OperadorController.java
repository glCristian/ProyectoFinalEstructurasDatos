package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.util.JsonConfig;
import com.techpark.model.Operador;
import com.techpark.service.AuthService;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperadorController {

    private final ParqueService parqueService = ParqueService.getInstance();
    private static final Gson gson = JsonConfig.GSON;

    public void registrarRutas(Javalin app) {

        // GET /operadores
        app.get("/operadores", ctx ->
            ctx.json(gson.toJson(parqueService.getOperadores())));

        // GET /operadores/{id}
        app.get("/operadores/{id}", ctx -> {
            Operador op = parqueService.getOperador(ctx.pathParam("id"));
            if (op == null) { ctx.status(404).result("Operador no encontrado"); return; }
            ctx.json(gson.toJson(op));
        });

        // POST /operadores → crear operador
        app.post("/operadores", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            String id = body.get("id");
            String nombre = body.get("nombre");
            String documento = body.get("documento");
            String username = body.get("username");
            String password = body.get("password");

            if (username == null || username.isBlank()) {
                username = documento;
            }

            String passwordHash = null;
            if (password != null && !password.isBlank()) {
                passwordHash = AuthService.hashPassword(password);
            } else if (documento != null && !documento.isBlank()) {
                passwordHash = AuthService.hashPassword(documento);
            }

            Operador op = new Operador(id, nombre, documento, username, passwordHash);
            boolean ok = parqueService.agregarOperador(op);
            if (ok && body.containsKey("zonaId"))
                parqueService.asignarOperadorAZona(body.get("id"), body.get("zonaId"));
            ctx.status(ok ? 201 : 409).result(ok ? "Operador creado" : "ID duplicado");
        });

        // PUT /operadores/{id}/zona → asignar zona
        app.put("/operadores/{id}/zona", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            boolean ok = parqueService.asignarOperadorAZona(ctx.pathParam("id"), body.get("zonaId"));
            ctx.status(ok ? 200 : 404).result(ok ? "Zona asignada" : "Operador o zona no encontrado");
        });

        // DELETE /operadores/{id}
        app.delete("/operadores/{id}", ctx -> {
            boolean ok = parqueService.eliminarOperador(ctx.pathParam("id"));
            ctx.status(ok ? 200 : 404).result(ok ? "Operador eliminado" : "No encontrado");
        });

        // GET /operadores/{id}/remocion-aviso → advertencia previa
        app.get("/operadores/{id}/remocion-aviso", ctx -> {
            String aviso = parqueService.advertenciaRemoverOperador(ctx.pathParam("id"));
            ctx.json(Map.of("aviso", aviso));
        });

        // POST /operadores/atracciones/{atraccionId}/procesar → procesar siguiente de la cola
        app.post("/operadores/atracciones/{atraccionId}/procesar", ctx -> {
            String atraccionId = ctx.pathParam("atraccionId");
            var lote = parqueService.procesarCiclo(atraccionId);
            if (lote.isEmpty()) { ctx.status(204).result("Cola vacía"); return; }
            List<Map<String, Object>> procesados = new ArrayList<>();
            for (var visitante : lote) {
                if (visitante == null) continue;
                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("id", visitante.getId());
                dto.put("nombre", visitante.getNombre());
                dto.put("edad", visitante.getEdad());
                dto.put("altura", visitante.getAltura());
                procesados.add(dto);
            }
            ctx.json(Map.of(
                "atraccionId", atraccionId,
                "cantidadProcesados", procesados.size(),
                "procesados", procesados
            ));
        });
    }
}
