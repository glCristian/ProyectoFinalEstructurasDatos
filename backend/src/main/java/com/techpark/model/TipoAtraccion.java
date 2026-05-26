package com.techpark.model;

/**
 * Clasificación de las atracciones del parque.
 * Las atracciones ACUATICA y MECANICA_ALTURA se cierran automáticamente
 * ante alertas de tormenta eléctrica o lluvia fuerte.
 */
public enum TipoAtraccion {
    ACUATICA,
    MECANICA_ALTURA,
    MECANICA_SUELO,
    SHOW,
    FAMILIAR,
    OTRO
}
