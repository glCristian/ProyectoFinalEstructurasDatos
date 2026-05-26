package com.techpark.service;

import com.techpark.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Servicio de carga de datos iniciales.
 * Permite poblar las estructuras del parque desde un archivo plano
 * o desde un escenario de prueba predefinido.
 *
 * Formato del archivo plano (líneas que empiezan con prefijo):
 *   ZONA;id;nombre;capacidad
 *   ATRACCION;id;nombre;tipo;capacidadCiclo;alturaMin;edadMin;costoAdicional;zonaId
 *   CONEXION;idA;idB;peso
 *   OPERADOR;id;nombre;documento;zonaId
 *   VISITANTE;id;nombre;documento;edad;altura;saldo;tipoTicket;precioTicket;[idsOtrosMiembros...]
 *   ADMIN;id;nombre;documento;clave
 */
public class CargaDatosService {

    private final ParqueService parqueService;

    public CargaDatosService(ParqueService parqueService) {
        this.parqueService = parqueService;
    }

    /**
     * Carga datos desde un archivo plano con el formato definido.
     *
     * @param rutaArchivo ruta absoluta o relativa al archivo.
     * @return número de entidades cargadas.
     */
    public int cargarDesdeArchivo(String rutaArchivo) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (procesarLinea(linea)) count++;
            }
        } catch (IOException e) {
            System.err.println("Error al leer archivo: " + e.getMessage());
        }
        return count;
    }

    /** Procesa una línea del archivo y la convierte en entidad del sistema. */
    private boolean procesarLinea(String linea) {
        String[] partes = linea.split(";");
        if (partes.length < 2) return false;
        try {
            switch (partes[0].toUpperCase()) {
                case "ZONA" -> {
                    Zona z = new Zona(partes[1], partes[2], Integer.parseInt(partes[3]));
                    return parqueService.agregarZona(z);
                }
                case "ATRACCION" -> {
                    Atraccion a = new Atraccion(
                        partes[1], partes[2],
                        TipoAtraccion.valueOf(partes[3]),
                        Integer.parseInt(partes[4]),
                        Double.parseDouble(partes[5]),
                        Integer.parseInt(partes[6]),
                        Double.parseDouble(partes[7]),
                        partes[8]
                    );
                    boolean ok = parqueService.agregarAtraccion(a);
                    if (ok) {
                        Zona z = parqueService.getZona(partes[8]);
                        if (z != null) z.agregarAtraccion(partes[1]);
                    }
                    return ok;
                }
                case "CONEXION" -> {
                    parqueService.conectarAtracciones(partes[1], partes[2], Double.parseDouble(partes[3]));
                    return true;
                }
                case "OPERADOR" -> {
                    String username = partes[3];
                    String passwordHash = AuthService.hashPassword(partes[3]);
                    if (partes.length > 5 && !partes[5].isBlank()) {
                        username = partes[5];
                    }
                    if (partes.length > 6 && !partes[6].isBlank()) {
                        passwordHash = AuthService.hashPassword(partes[6]);
                    }

                    Operador op = new Operador(partes[1], partes[2], partes[3], username, passwordHash);
                    boolean ok  = parqueService.agregarOperador(op);
                    if (ok && partes.length > 4 && !partes[4].isBlank())
                        parqueService.asignarOperadorAZona(partes[1], partes[4]);
                    return ok;
                }
                case "VISITANTE" -> {
                    Visitante v = new Visitante(partes[1], partes[2], partes[3],
                        Integer.parseInt(partes[4]), Double.parseDouble(partes[5]));
                    v.setSaldoVirtual(Double.parseDouble(partes[6]));
                    boolean ok = parqueService.registrarVisitante(v);
                    if (!ok) return false;

                    TipoTicket tipo = TipoTicket.valueOf(partes[7]);
                    switch (tipo) {
                        case GENERAL -> parqueService.comprarTicketGeneral(partes[1]);
                        case FAST_PASS -> parqueService.comprarTicketFastPass(partes[1]);
                        case FAMILIAR -> {
                            if (partes.length > 9) {
                                java.util.List<String> idsMiembros = new java.util.ArrayList<>();
                                for (int i = 9; i < partes.length; i++) {
                                    if (!partes[i].isBlank()) idsMiembros.add(partes[i]);
                                }
                                parqueService.comprarTicketFamiliar(partes[1], idsMiembros.size() + 1, idsMiembros);
                            } else {
                                parqueService.comprarTicketGeneral(partes[1]);
                            }
                        }
                    }
                    return true;
                }
                case "ADMIN" -> {
                    Administrador admin = new Administrador(partes[1], partes[2], partes[3], partes[4]);
                    return parqueService.agregarAdmin(admin);
                }
                default -> { return false; }
            }
        } catch (Exception e) {
            System.err.println("Error procesando línea: " + linea + " → " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga un escenario de prueba predefinido en memoria.
     * Se invoca desde la GUI con el botón "Cargar datos de prueba".
     */
    public void cargarEscenarioPrueba() {
        // ── Zonas ─────────────────────────────────────────────────────
        parqueService.agregarZona(new Zona("Z1", "Zona Acuática",    300));
        parqueService.agregarZona(new Zona("Z2", "Zona Mecánica",    250));
        parqueService.agregarZona(new Zona("Z3", "Zona Familiar",    400));
        parqueService.agregarZona(new Zona("Z4", "Zona Shows",       500));

        // ── Atracciones ───────────────────────────────────────────────
        agregarAtraccionConZona("A1", "Tobogán Extremo",   TipoAtraccion.ACUATICA,        20, 1.40, 12, 0.0,  "Z1", 498);
        agregarAtraccionConZona("A2", "Ola Gigante",       TipoAtraccion.ACUATICA,        30, 1.20, 10, 5000, "Z1", 499);
        agregarAtraccionConZona("A3", "Montaña Rusa",      TipoAtraccion.MECANICA_ALTURA, 24, 1.45, 14, 8000, "Z2", 497);
        agregarAtraccionConZona("A4", "Caída Libre",       TipoAtraccion.MECANICA_ALTURA, 12, 1.50, 16, 10000,"Z2", 499);
        agregarAtraccionConZona("A5", "Carros Chocones",   TipoAtraccion.MECANICA_SUELO,  16, 1.10,  8, 3000, "Z3", 498);
        agregarAtraccionConZona("A6", "Carrusel Mágico",   TipoAtraccion.FAMILIAR,        20, 0.80,  4, 0.0,  "Z3", 497);
        agregarAtraccionConZona("A7", "Show Láser",        TipoAtraccion.SHOW,            200,0.0,   0, 0.0,  "Z4", 499);
        agregarAtraccionConZona("A8", "Teatro Acuático",   TipoAtraccion.SHOW,            150,0.0,   0, 5000, "Z4", 498);

        // ── Conexiones del grafo ──────────────────────────────────────
        parqueService.conectarAtracciones("A1", "A2", 50);
        parqueService.conectarAtracciones("A1", "A5", 120);
        parqueService.conectarAtracciones("A2", "A3", 80);
        parqueService.conectarAtracciones("A3", "A4", 30);
        parqueService.conectarAtracciones("A3", "A7", 150);
        parqueService.conectarAtracciones("A4", "A5", 90);
        parqueService.conectarAtracciones("A5", "A6", 40);
        parqueService.conectarAtracciones("A6", "A7", 70);
        parqueService.conectarAtracciones("A7", "A8", 20);
        parqueService.conectarAtracciones("A8", "A1", 200);

        // ── Operadores ────────────────────────────────────────────────
        Operador op1 = new Operador("OP1", "Carlos Rueda", "1001", "1001", AuthService.hashPassword("1001"));
        Operador op2 = new Operador("OP2", "María López", "1002", "1002", AuthService.hashPassword("1002"));
        Operador op3 = new Operador("OP3", "Juan Sosa",   "1003", "1003", AuthService.hashPassword("1003"));
        Operador op4 = new Operador("OP4", "Ana Torres",  "1004", "1004", AuthService.hashPassword("1004"));
        parqueService.agregarOperador(op1);
        parqueService.agregarOperador(op2);
        parqueService.agregarOperador(op3);
        parqueService.agregarOperador(op4);
        parqueService.asignarOperadorAZona("OP1", "Z1");
        parqueService.asignarOperadorAZona("OP2", "Z2");
        parqueService.asignarOperadorAZona("OP3", "Z3");
        parqueService.asignarOperadorAZona("OP4", "Z4");

        // ── Administrador ─────────────────────────────────────────────
        parqueService.agregarAdmin(new Administrador("ADM1", "Director General", "9999", "admin123"));

        // ── Visitantes de prueba ──────────────────────────────────────
        Visitante v1 = new Visitante("V1", "Pedro Ramos", "555001", 25, 1.75);
        v1.setSaldoVirtual(50000);
        parqueService.registrarVisitante(v1);
        parqueService.comprarTicketFastPass("V1");

        Visitante v2 = new Visitante("V2", "Laura Díaz", "555002", 17, 1.60);
        v2.setSaldoVirtual(20000);
        parqueService.registrarVisitante(v2);
        parqueService.comprarTicketGeneral("V2");

        Visitante v3 = new Visitante("V3", "Familia García", "555003", 35, 1.65);
        Visitante v4 = new Visitante("V4", "Hija García", "555004", 12, 1.40);
        Visitante v5 = new Visitante("V5", "Hijo García", "555005", 10, 1.35);
        Visitante v6 = new Visitante("V6", "Abuela García", "555006", 60, 1.58);
        v3.setSaldoVirtual(120000);
        parqueService.registrarVisitante(v3);
        parqueService.registrarVisitante(v4);
        parqueService.registrarVisitante(v5);
        parqueService.registrarVisitante(v6);
        parqueService.comprarTicketFamiliar("V3", 4, java.util.List.of("V4", "V5", "V6"));

        parqueService.asignarPosicionesMapaPorDefectoSiHaceFalta();

        System.out.println("✅  Escenario de prueba cargado correctamente.");
    }

    private void agregarAtraccionConZona(String id, String nombre, TipoAtraccion tipo,
                                          int cap, double altMin, int edadMin,
                                          double costo, String zonaId, int ciclosCompletados) {
        Atraccion a = new Atraccion(id, nombre, tipo, cap, altMin, edadMin, costo, zonaId);
        a.setCiclosCompletados(ciclosCompletados);
        parqueService.agregarAtraccion(a);
        Zona z = parqueService.getZona(zonaId);
        if (z != null) z.agregarAtraccion(id);
    }
}
