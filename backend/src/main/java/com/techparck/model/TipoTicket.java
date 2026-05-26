package com.techpark.model;

/**
 * Tipos de entrada disponibles en el parque.
 * - GENERAL   : ingreso normal al parque.
 * - FAMILIAR  : ingreso con descuento bajo condiciones de la administración.
 * - FAST_PASS : prioridad en la cola virtual de determinadas atracciones.
 */
public enum TipoTicket {
    GENERAL,
    FAMILIAR,
    FAST_PASS
}
