package com.techpark.service;

import com.techpark.model.Administrador;
import com.techpark.model.Operador;
import com.techpark.model.Rol;
import com.techpark.model.Sesion;
import com.techpark.model.Visitante;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

	private static final int HORAS_EXPIRACION = 8;

	private final ParqueService parqueService;
	private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

	public AuthService() {
		this(ParqueService.getInstance());
	}

	public AuthService(ParqueService parqueService) {
		this.parqueService = parqueService;
	}

	public Sesion login(String username, String password) {
		if (username == null || username.isBlank() || password == null) {
			return null;
		}

		Administrador admin = buscarAdministrador(username);
		if (admin != null && passwordMatches(password, admin.getPasswordHash())) {
			return crearSesion(admin.getId(), Rol.ADMIN);
		}

		Operador operador = buscarOperador(username);
		if (operador != null && passwordMatches(password, operador.getPasswordHash())) {
			return crearSesion(operador.getId(), Rol.OPERADOR);
		}

		Visitante visitante = buscarVisitante(username);
		if (visitante != null && passwordMatches(password, visitante.getPasswordHash())) {
			return crearSesion(visitante.getId(), Rol.VISITANTE);
		}

		return null;
	}

	public static String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 no disponible", e);
		}
	}


	public boolean logout(String token) {
		return token != null && sesiones.remove(token) != null;
	}

	public Sesion validarToken(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}

		Sesion sesion = sesiones.get(token);
		if (sesion == null) {
			return null;
		}

		if (sesion.getFechaExpiracion() != null && sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
			sesiones.remove(token);
			return null;
		}

		return sesion;
	}

	public Rol getRolDeToken(String token) {
		Sesion sesion = validarToken(token);
		if (sesion == null || sesion.getRol() == null) {
			return null;
		}

		try {
			return Rol.valueOf(sesion.getRol());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private Sesion crearSesion(String usuarioId, Rol rol) {
		Sesion sesion = new Sesion(usuarioId, rol.name(), LocalDateTime.now().plusHours(HORAS_EXPIRACION));
		sesiones.put(sesion.getToken(), sesion);
		return sesion;
	}

	private Administrador buscarAdministrador(String username) {
		for (Administrador admin : parqueService.getAdmins()) {
			if (coincideUsuario(admin.getId(), username) || coincideUsuario(admin.getDocumento(), username)) {
				return admin;
			}
		}
		return null;
	}

	private Operador buscarOperador(String username) {
		for (Operador operador : parqueService.getOperadores()) {
			if (coincideUsuario(operador.getUsername(), username)
				|| coincideUsuario(operador.getId(), username)
				|| coincideUsuario(operador.getDocumento(), username)) {
				return operador;
			}
		}
		return null;
	}

	private Visitante buscarVisitante(String username) {
		for (Visitante visitante : parqueService.getVisitantes()) {
			if (coincideUsuario(visitante.getUsername(), username)
				|| coincideUsuario(visitante.getId(), username)
				|| coincideUsuario(visitante.getDocumento(), username)) {
				return visitante;
			}
		}
		return null;
	}

	private boolean coincideUsuario(String valor, String username) {
		return valor != null && valor.equals(username);
	}

	private boolean passwordMatches(String rawPassword, String storedPassword) {
		if (storedPassword == null) {
			return false;
		}

		return storedPassword.equals(rawPassword) || storedPassword.equals(hashPassword(rawPassword));
	}



}
