package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.model.Ticket;
import com.techpark.model.TipoTicket;
import com.techpark.model.Visitante;
import com.techpark.service.ParqueService;
import com.techpark.service.PersistenciaService;
import com.techpark.util.JsonConfig;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitanteController {

    private final ParqueService parqueService = ParqueService.getInstance();
    private final PersistenciaService persistenciaService = new PersistenciaService();
    private static final Gson gson = JsonConfig.GSON;

    public void registrarRutas(Javalin app) {
        app.get("/visitantes", ctx -> ctx.result(gson.toJson(parqueService.getVisitantes().stream().map(this::visitanteDto).toList())));

        app.get("/visitantes/{id}", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) {
                ctx.status(404).result("Visitante no encontrado");
                return;
            }
            ctx.result(gson.toJson(visitanteDto(visitante)));
        });

        app.post("/visitantes", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            try {
                Visitante visitante = new Visitante(
                    (String) body.get("id"),
                    (String) body.get("nombre"),
                    (String) body.get("documento"),
                    ((Number) body.get("edad")).intValue(),
                    ((Number) body.get("altura")).doubleValue()
                );
                if (body.containsKey("saldoVirtual")) {
                    visitante.setSaldoVirtual(((Number) body.get("saldoVirtual")).doubleValue());
                }
                if (body.get("username") instanceof String username && !username.isBlank()) {
                    visitante.setUsername(username);
                }
                if (body.get("passwordHash") instanceof String passwordHash && !passwordHash.isBlank()) {
                    visitante.setPasswordHash(passwordHash);
                }
                boolean ok = parqueService.registrarVisitante(visitante);
                ctx.status(ok ? 201 : 409).result(ok ? "Visitante registrado" : "ID duplicado o parque sin cupo");
            } catch (Exception e) {
                ctx.status(400).result("Datos inválidos: " + e.getMessage());
            }
        });

        app.put("/visitantes/{id}", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) {
                ctx.status(404).result("No encontrado");
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);

            if (body.containsKey("nombre")) {
                String nombre = (String) body.get("nombre");
                if (nombre != null && !nombre.isBlank()) visitante.setNombre(nombre);
            }
            if (body.containsKey("documento")) {
                String documento = (String) body.get("documento");
                if (documento != null && !documento.isBlank()) visitante.setDocumento(documento);
            }
            if (body.containsKey("edad")) {
                visitante.setEdad(((Number) body.get("edad")).intValue());
            }
            if (body.containsKey("altura")) {
                visitante.setAltura(((Number) body.get("altura")).doubleValue());
            }
            if (body.containsKey("username")) {
                String username = (String) body.get("username");
                if (username != null && !username.isBlank()) visitante.setUsername(username);
            }

            ctx.result("Perfil actualizado");
        });

        app.delete("/visitantes/{id}", ctx -> {
            boolean ok = parqueService.retirarVisitante(ctx.pathParam("id"));
            if (ok) persistenciaService.guardarTodo(parqueService);
            ctx.status(ok ? 200 : 404).result(ok ? "Visitante eliminado" : "Visitante no encontrado");
        });

        app.get("/visitantes/{id}/ticket", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) {
                ctx.status(404).result("No encontrado");
                return;
            }
            Ticket ticket = visitante.getTicketComprado();
            if (ticket == null) {
                ctx.status(404).result("El visitante no tiene ticket comprado");
                return;
            }
            ctx.result(gson.toJson(ticketDto(ticket)));
        });

        app.post("/tickets/comprar", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            String visitanteId = body != null ? (String) body.get("visitanteId") : null;
            String tipoRaw = body != null ? (String) body.get("tipoTicket") : null;
            if (visitanteId == null || visitanteId.isBlank() || tipoRaw == null || tipoRaw.isBlank()) {
                ctx.status(400).result("visitanteId y tipoTicket son requeridos");
                return;
            }

            TipoTicket tipoTicket;
            try {
                tipoTicket = TipoTicket.valueOf(tipoRaw.toUpperCase());
            } catch (IllegalArgumentException e) {
                ctx.status(400).result("Tipo de ticket inválido");
                return;
            }

            Ticket ticket;
            switch (tipoTicket) {
                case GENERAL -> ticket = parqueService.comprarTicketGeneral(visitanteId);
                case FAST_PASS -> ticket = parqueService.comprarTicketFastPass(visitanteId);
                case FAMILIAR -> {
                    ticket = comprarTicketFamiliarDesdeBody(visitanteId, body);
                }
                default -> ticket = null;
            }

            if (ticket == null) {
                ctx.status(400).result("No se pudo comprar el ticket. Verifica saldo, datos o si ya tienes una entrada activa.");
                return;
            }

            persistenciaService.guardarTodo(parqueService);

            ctx.status(201).result(gson.toJson(Map.of(
                "ticketId", ticket.getId(),
                "tipo", ticket.getTipo(),
                "numeroPersonasIncluidas", ticket.getNumeroPersonasIncluidas(),
                "precioFinal", ticket.getPrecioCompra(),
                "activo", ticket.isActivo(),
                "fechaCompra", ticket.getFechaCompra(),
                "miembros", miembrosDto(ticket)
            )));
        });

        app.post("/visitantes/{id}/saldo/recargar", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) {
                ctx.status(404).result("No encontrado");
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            double monto = body != null && body.get("monto") instanceof Number ? ((Number) body.get("monto")).doubleValue() : 0.0;
            if (monto <= 0) {
                ctx.status(400).result("Monto inválido");
                return;
            }
            double saldoAnterior = visitante.getSaldoVirtual();
            parqueService.recargarSaldo(visitante.getId(), monto);
            ctx.result(gson.toJson(Map.of(
                "saldoAnterior", saldoAnterior,
                "monto", monto,
                "saldoNuevo", visitante.getSaldoVirtual()
            )));
        });

        app.post("/visitantes/{id}/saldo", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) {
                ctx.status(404).result("No encontrado");
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            double monto = body != null && body.get("monto") instanceof Number ? ((Number) body.get("monto")).doubleValue() : 0.0;
            if (monto <= 0) {
                ctx.status(400).result("Monto inválido");
                return;
            }
            double saldoAnterior = visitante.getSaldoVirtual();
            parqueService.recargarSaldo(visitante.getId(), monto);
            ctx.result(gson.toJson(Map.of(
                "saldoAnterior", saldoAnterior,
                "monto", monto,
                "saldoNuevo", visitante.getSaldoVirtual()
            )));
        });

        app.put("/visitantes/{id}/ticket", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
            String tipoRaw = body != null ? (String) body.get("tipoTicket") : null;
            if (tipoRaw == null || tipoRaw.isBlank()) {
                ctx.status(400).result("tipoTicket es requerido");
                return;
            }
            String visitanteId = ctx.pathParam("id");
            TipoTicket tipoTicket;
            try {
                tipoTicket = TipoTicket.valueOf(tipoRaw.toUpperCase());
            } catch (IllegalArgumentException e) {
                ctx.status(400).result("Tipo de ticket inválido");
                return;
            }

            Ticket ticket;
            switch (tipoTicket) {
                case GENERAL -> ticket = parqueService.comprarTicketGeneral(visitanteId);
                case FAST_PASS -> ticket = parqueService.comprarTicketFastPass(visitanteId);
                case FAMILIAR -> {
                    ticket = comprarTicketFamiliarDesdeBody(visitanteId, body);
                }
                default -> ticket = null;
            }

            if (ticket == null) {
                ctx.status(400).result("No se pudo actualizar/comprar el ticket");
                return;
            }
            persistenciaService.guardarTodo(parqueService);
            ctx.result(gson.toJson(ticketDto(ticket)));
        });

        app.delete("/visitantes/{id}/ticket", ctx -> {
            boolean ok = parqueService.cancelarTicket(ctx.pathParam("id"));
            if (ok) persistenciaService.guardarTodo(parqueService);
            ctx.status(ok ? 200 : 404).result(ok ? "Ticket cancelado" : "No encontrado");
        });

        app.post("/visitantes/{id}/cola/{atraccionId}", ctx -> {
            ParqueService.ResultadoIngresoCola resultado = parqueService.ingresarACola(
                ctx.pathParam("id"),
                ctx.pathParam("atraccionId")
            );
            ctx.status(resultado.isExitoso() ? 200 : 400).result(gson.toJson(resultado));
        });

        app.get("/visitantes/{id}/cola/{atraccionId}", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            String atraccionId = ctx.pathParam("atraccionId");
            int posicion = parqueService.posicionEnCola(visitante.getId(), atraccionId);
            int tamanio = parqueService.tamanioCola(atraccionId);
            double espera = parqueService.tiempoEsperaParaPosicion(atraccionId, posicion);
            ctx.result(gson.toJson(Map.of(
                "atraccionId", atraccionId,
                "posicion", posicion,
                "tamanio", tamanio,
                "esperaMin", espera
            )));
        });

        app.get("/visitantes/{id}/historial", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            ctx.result(gson.toJson(visitante.getHistorialVisitas().toList()));
        });

        app.get("/visitantes/{id}/favoritos", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            ctx.result(gson.toJson(visitante.getFavoritos().toSet()));
        });

        app.post("/visitantes/{id}/favoritos/{atraccionId}", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            boolean ok = visitante.agregarFavorito(ctx.pathParam("atraccionId"));
            ctx.result(ok ? "Favorito agregado" : "Ya existe en favoritos");
        });

        app.delete("/visitantes/{id}/favoritos/{atraccionId}", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            boolean ok = visitante.eliminarFavorito(ctx.pathParam("atraccionId"));
            ctx.status(ok ? 200 : 404).result(ok ? "Favorito eliminado" : "No encontrado en favoritos");
        });

        app.get("/visitantes/{id}/notificaciones", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            ctx.result(gson.toJson(parqueService.getNotificaciones(visitante.getId())));
        });

        app.delete("/visitantes/{id}/notificaciones", ctx -> {
            Visitante visitante = parqueService.getVisitante(ctx.pathParam("id"));
            if (visitante == null) { ctx.status(404).result("No encontrado"); return; }
            parqueService.limpiarNotificaciones(visitante.getId());
            ctx.status(200).result("Notificaciones limpiadas");
        });
    }

    private Map<String, Object> visitanteDto(Visitante visitante) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", visitante.getId());
        dto.put("nombre", visitante.getNombre());
        dto.put("documento", visitante.getDocumento());
        dto.put("edad", visitante.getEdad());
        dto.put("altura", visitante.getAltura());
        dto.put("saldoVirtual", visitante.getSaldoVirtual());
        dto.put("username", visitante.getUsername());
        dto.put("puedeDentrar", visitante.isPuedeDentrar());
        dto.put("numeroPersonasFamilia", visitante.getNumeroPersonasFamilia());
        dto.put("liderGrupoId", visitante.getLiderGrupo() != null ? visitante.getLiderGrupo().getId() : null);
        dto.put("ticket", visitante.getTicketComprado() != null ? ticketDto(visitante.getTicketComprado()) : null);
        dto.put("miembrosGrupoFamiliar", miembrosDto(visitante.getTicketComprado()));
        return dto;
    }

    private Map<String, Object> ticketDto(Ticket ticket) {
        if (ticket == null) return null;
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", ticket.getId());
        dto.put("tipo", ticket.getTipo());
        dto.put("visitanteId", ticket.getVisitanteId());
        dto.put("numeroPersonasIncluidas", ticket.getNumeroPersonasIncluidas());
        dto.put("precioCompra", ticket.getPrecioCompra());
        dto.put("precio", ticket.getPrecioCompra());
        dto.put("activo", ticket.isActivo());
        dto.put("fechaCompra", ticket.getFechaCompra());
        dto.put("fechaEmision", ticket.getFechaEmision());
        dto.put("fechaVencimiento", ticket.getFechaVencimiento());
        dto.put("miembrosDeLaFamilia", miembrosDto(ticket));
        return dto;
    }

    private List<Map<String, Object>> miembrosDto(Ticket ticket) {
        List<Map<String, Object>> miembros = new ArrayList<>();
        if (ticket == null || ticket.getMiembrosDeLaFamilia() == null) return miembros;
        for (Visitante miembro : ticket.getMiembrosDeLaFamilia()) {
            if (miembro == null) continue;
            miembros.add(Map.of(
                "id", miembro.getId(),
                "nombre", miembro.getNombre(),
                "edad", miembro.getEdad(),
                "altura", miembro.getAltura()
            ));
        }
        return miembros;
    }

    private Ticket comprarTicketFamiliarDesdeBody(String visitanteId, Map<String, Object> body) {
        int numeroPersonas = body != null && body.get("numeroPersonas") instanceof Number
            ? ((Number) body.get("numeroPersonas")).intValue() : 0;

        boolean tieneMiembrosFamilia = body != null && body.get("miembrosFamilia") instanceof List<?>;

        List<Visitante> miembrosInternos = new ArrayList<>();
        if (tieneMiembrosFamilia && body.get("miembrosFamilia") instanceof List<?> listaMiembros) {
            for (int i = 0; i < listaMiembros.size(); i++) {
                Object valor = listaMiembros.get(i);
                if (valor instanceof Map<?, ?> mapaMiembro) {
                    Visitante miembro = construirMiembroFamiliarInterno(mapaMiembro, visitanteId, i);
                    if (miembro == null) return null;
                    miembrosInternos.add(miembro);
                }
            }
        }

        if (miembrosInternos.isEmpty() && body != null && body.get("idsOtrosMiembros") instanceof List<?> listaIds) {
            for (Object valor : listaIds) {
                if (valor == null) continue;
                String idMiembro = valor.toString().trim();
                if (idMiembro.isBlank()) continue;
                Visitante miembro = parqueService.getVisitante(idMiembro);
                if (miembro == null) return null;
                miembrosInternos.add(miembro);
            }
        }

        if (numeroPersonas <= 0) {
            numeroPersonas = miembrosInternos.size() + 1;
        }

        if (miembrosInternos.size() != numeroPersonas - 1) {
            return null;
        }

        if (tieneMiembrosFamilia) {
            return parqueService.comprarTicketFamiliarConMiembros(visitanteId, numeroPersonas, miembrosInternos);
        }

        List<String> idsOtrosMiembros = new ArrayList<>();
        for (Visitante miembro : miembrosInternos) {
            idsOtrosMiembros.add(miembro.getId());
        }
        return parqueService.comprarTicketFamiliar(visitanteId, numeroPersonas, idsOtrosMiembros);
    }

    private Visitante construirMiembroFamiliarInterno(Map<?, ?> datosMiembro, String visitanteId, int indice) {
        String nombre = obtenerTexto(datosMiembro.get("nombre"));
        int edad = obtenerEntero(datosMiembro.get("edad"));
        double altura = obtenerDouble(datosMiembro.get("altura"));

        if (nombre == null || nombre.isBlank() || edad < 0 || altura <= 0) {
            return null;
        }

        String id = "FAM-" + visitanteId + "-" + (indice + 1);
        return new Visitante(id, nombre, id, edad, altura);
    }

    private String obtenerTexto(Object valor) {
        return valor instanceof String texto ? texto.trim() : null;
    }

    private int obtenerEntero(Object valor) {
        return valor instanceof Number numero ? numero.intValue() : -1;
    }

    private double obtenerDouble(Object valor) {
        return valor instanceof Number numero ? numero.doubleValue() : -1.0;
    }
}
