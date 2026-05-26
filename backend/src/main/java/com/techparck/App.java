package main.java.com.techparck;

import com.techpark.controller.*;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
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

        // ── Registrar controladores ────────────────────────────────────
        AtraccionController atraccionController = new AtraccionController();
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
}
