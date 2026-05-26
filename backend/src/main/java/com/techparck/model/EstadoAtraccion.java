package com.techpark.model;

/**
 * Estados posibles de una atracción del parque.
 * - ACTIVA          : operativa y disponible para visitantes.
 * - EN_MANTENIMIENTO: bloqueada por revisión técnica (ej. >= 500 visitantes).
 * - CERRADA         : fuera de servicio (clima adverso, cierre técnico, etc.).
 */
public enum EstadoAtraccion {
    ACTIVA,
    EN_MANTENIMIENTO,
    CERRADA
}
