package com.techpark;

import com.techpark.controller.*;
import com.techpark.model.Rol;
import com.techpark.model.Sesion;
import com.techpark.service.AuthService;
import com.techpark.service.ParqueService;
import com.techpark.service.PersistenciaService;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {
        ParqueService parqueService = ParqueService.getInstance();
        PersistenciaService persistenciaService = new PersistenciaService();
        AuthController authController = new AuthController();
        AuthService authService = authController.getAuthService();

        Javalin app = Javalin.create(config -> {
            // Habilitar CORS para el frontend React
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.anyHost();
                });
            });
        }).start(8080);

        // ── Ruta de salud ──────────────────────────────────────────────
        app.get("/ping", ctx -> ctx.result("pong"));

        persistenciaService.cargarTodo(parqueService);
        persistenciaService.guardarTodo(parqueService);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> persistenciaService.guardarTodo(ParqueService.getInstance()),
                60, 60, TimeUnit.SECONDS);

        
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> persistenciaService.guardarTodo(ParqueService.getInstance())));


        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            persistenciaService.guardarTodo(parqueService)
        ));

        app.before("/admin/*", ctx -> {
            String token = extraerToken(ctx.header("Authorization"));
            Sesion sesion = authService.validarToken(token);
            if (sesion == null || sesion.getRol() == null || !Rol.ADMIN.name().equalsIgnoreCase(sesion.getRol())) {
                throw new UnauthorizedResponse("Acceso denegado");
            }
        });

        app.before("/operadores/*", ctx -> {
            String token = extraerToken(ctx.header("Authorization"));
            Sesion sesion = authService.validarToken(token);
            if (sesion == null || sesion.getRol() == null) {
                throw new UnauthorizedResponse("Acceso denegado");
            }

            String rol = sesion.getRol().toUpperCase(Locale.ROOT);
            if (!Rol.ADMIN.name().equals(rol) && !Rol.OPERADOR.name().equals(rol)) {
                throw new UnauthorizedResponse("Acceso denegado");
            }
        });

        // ── Registrar controladores ────────────────────────────────────
        AtraccionController atraccionController = new AtraccionController();
        authController.registrarRutas(app);
        VisitanteController visitanteController = new VisitanteController();
        OperadorController operadorController   = new OperadorController();
        AdminController    adminController      = new AdminController();
        ClimaController    climaController      = new ClimaController();
        RutaController     rutaController       = new RutaController();
        ZonaController     zonaController       = new ZonaController();

        atraccionController.registrarRutas(app);
        visitanteController.registrarRutas(app);
        operadorController.registrarRutas(app);
        adminController.registrarRutas(app);
        climaController.registrarRutas(app);
        rutaController.registrarRutas(app);
        zonaController.registrarRutas(app);

        System.out.println("✅  Tech-Park UQ backend corriendo en http://localhost:8080");
    }

    private static String extraerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }

        return authorizationHeader.trim();
    }
}
