package com.techpark.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfiguracionTicket {

    public static final int minimoPersonasFamilia = 2;
    public static final int maximoPersonasFamilia = 5;

    public static final double precioGeneral = 40000.0;
    public static final double precioFastPass = 65000.0;

    public static final Map<Integer, Double> preciosPorFamiliar;
    public static final Map<String, Double> costosAdicionalesAtracciones;

    static {
        Map<Integer, Double> preciosFamilia = new LinkedHashMap<>();
        preciosFamilia.put(2, 65000.0);
        preciosFamilia.put(3, 85000.0);
        preciosFamilia.put(4, 100000.0);
        preciosFamilia.put(5, 110000.0);
        preciosPorFamiliar = Collections.unmodifiableMap(preciosFamilia);

        Map<String, Double> costos = new LinkedHashMap<>();
        costos.put("A2", 5000.0);
        costos.put("A3", 8000.0);
        costos.put("A4", 10000.0);
        costos.put("A8", 5000.0);
        costosAdicionalesAtracciones = Collections.unmodifiableMap(costos);
    }

    private ConfiguracionTicket() {}

    public static double obtenerPrecioFamiliar(int numeroPersonas) {
        return preciosPorFamiliar.getOrDefault(numeroPersonas, 0.0);
    }

    public static double obtenerCostoAtraccion(String atraccionId, double costoDefecto) {
        return costosAdicionalesAtracciones.getOrDefault(atraccionId, costoDefecto);
    }
}