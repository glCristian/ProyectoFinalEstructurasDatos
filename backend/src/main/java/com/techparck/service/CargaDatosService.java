package main.java.com.techparck.service;

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
 *   VISITANTE;id;nombre;documento;edad;altura;saldo;tipoTicket;precioTicket
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
                    Operador op = new Operador(partes[1], partes[2], partes[3]);
                    boolean ok  = parqueService.agregarOperador(op);
                    if (ok && partes.length > 4 && !partes[4].isBlank())
                        parqueService.asignarOperadorAZona(partes[1], partes[4]);
                    return ok;
                }
                case "VISITANTE" -> {
                    Visitante v = new Visitante(partes[1], partes[2], partes[3],
                        Integer.parseInt(partes[4]), Double.parseDouble(partes[5]));
                    v.setSaldoVirtual(Double.parseDouble(partes[6]));
                    TipoTicket tipo   = TipoTicket.valueOf(partes[7]);
                    double precioTkt  = Double.parseDouble(partes[8]);
                    return parqueService.registrarVisitante(v, tipo, precioTkt);
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
}
