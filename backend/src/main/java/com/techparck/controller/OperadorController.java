package main.java.com.techparck.controller;

public class OperadorController {
    private final ParqueService parqueService = ParqueService.getInstance();
    private final Gson gson = new Gson();

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
            Operador op = new Operador(body.get("id"), body.get("nombre"), body.get("documento"));
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

        // POST /operadores/atracciones/{atraccionId}/procesar → procesar siguiente de la cola
        app.post("/operadores/atracciones/{atraccionId}/procesar", ctx -> {
            String atraccionId = ctx.pathParam("atraccionId");
            Visitante v = parqueService.procesarSiguiente(atraccionId);
            if (v == null) { ctx.status(204).result("Cola vacía"); return; }
            ctx.json(gson.toJson(v));
        });
    }
}
