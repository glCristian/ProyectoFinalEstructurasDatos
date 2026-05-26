package com.techpark.controller;

import com.google.gson.Gson;
import com.techpark.util.JsonConfig;
import com.techpark.model.Administrador;
import com.techpark.model.Operador;
import com.techpark.model.Rol;
import com.techpark.model.Sesion;
import com.techpark.model.Visitante;
import com.techpark.service.AuthService;
import com.techpark.service.ParqueService;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;

import java.util.HashMap;
import java.util.Map;

public class AuthController {

	private final AuthService authService = new AuthService();
	private final ParqueService parqueService = ParqueService.getInstance();
	private static final Gson gson = JsonConfig.GSON;

	public void registrarRutas(Javalin app) {
		app.post("/auth/login", ctx -> {
			@SuppressWarnings("unchecked")
			Map<String, String> body = gson.fromJson(ctx.body(), Map.class);

			String username = body != null ? body.get("username") : null;
			String password = body != null ? body.get("password") : null;

			Sesion sesion = authService.login(username, password);
			if (sesion == null) {
				throw new UnauthorizedResponse("Credenciales inválidas");
			}

			ctx.json(Map.of(
				"token", sesion.getToken(),
				"rol", sesion.getRol(),
				"userId", sesion.getUsuarioId()
			));
		});

		app.post("/auth/logout", ctx -> {
			String token = extraerToken(ctx.header("Authorization"));
			if (token == null || !authService.logout(token)) {
				throw new UnauthorizedResponse("Token inválido");
			}
			ctx.status(200);
		});

		app.post("/auth/registro", ctx -> {
			@SuppressWarnings("unchecked")
			Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);

			String nombre = body != null ? (String) body.get("nombre") : null;
			String username = body != null ? (String) body.get("username") : null;
			String password = body != null ? (String) body.get("password") : null;
			String documento = body != null ? (String) body.get("documento") : null;
			int edad = body != null && body.get("edad") instanceof Number ? ((Number) body.get("edad")).intValue() : 0;
			double altura = body != null && body.get("altura") instanceof Number
				? ((Number) body.get("altura")).doubleValue() : 0.0;
			double saldoInicial = body != null && body.get("saldoInicial") instanceof Number
				? ((Number) body.get("saldoInicial")).doubleValue() : 0.0;

			if (nombre == null || nombre.isBlank()
				|| username == null || username.isBlank()
				|| password == null || password.isBlank()
				|| documento == null || documento.isBlank()) {
				ctx.status(400).result("Datos incompletos");
				return;
			}

			Visitante visitante = new Visitante(
				username,
				nombre,
				documento,
				edad,
				altura,
				username,
				AuthService.hashPassword(password)
			);
			visitante.setSaldoVirtual(saldoInicial);

			boolean ok = parqueService.registrarVisitante(visitante);
			if (!ok) {
				ctx.status(409).result("No se pudo registrar visitante");
				return;
			}

			Sesion sesion = authService.login(username, password);
			if (sesion == null) {
				throw new UnauthorizedResponse("Credenciales inválidas");
			}

			ctx.status(201).json(Map.of(
				"token", sesion.getToken(),
				"rol", sesion.getRol(),
				"userId", sesion.getUsuarioId()
			));
		});

		app.get("/auth/me", ctx -> {
			String token = extraerToken(ctx.header("Authorization"));
			Sesion sesion = authService.validarToken(token);
			if (sesion == null) {
				throw new UnauthorizedResponse("Token inválido o expirado");
			}

			Map<String, Object> response = new HashMap<>();
			response.put("id", sesion.getUsuarioId());
			response.put("nombre", obtenerNombreUsuario(sesion));
			response.put("rol", sesion.getRol());
			ctx.json(response);
		});
	}

	public AuthService getAuthService() {
		return authService;
	}

	private String extraerToken(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return null;
		}

		if (authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring("Bearer ".length()).trim();
		}

		return authorizationHeader.trim();
	}

	private String obtenerNombreUsuario(Sesion sesion) {
		Rol rol;
		try {
			rol = Rol.valueOf(sesion.getRol());
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}

		return switch (rol) {
			case ADMIN -> {
				Administrador admin = parqueService.getAdmin(sesion.getUsuarioId());
				yield admin != null ? admin.getNombre() : null;
			}
			case OPERADOR -> {
				Operador operador = parqueService.getOperador(sesion.getUsuarioId());
				yield operador != null ? operador.getNombre() : null;
			}
			case VISITANTE -> {
				Visitante visitante = parqueService.getVisitante(sesion.getUsuarioId());
				yield visitante != null ? visitante.getNombre() : null;
			}
		};
	}
}
