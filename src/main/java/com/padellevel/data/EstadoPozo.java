package com.padellevel.data;

/**
 * Estados posibles de un pozo (pool).
 */
public enum EstadoPozo {
    /**
     * Inscripción abierta - Los jugadores pueden inscribirse.
     */
    INSCRIPCION_ABIERTA("Inscripción Abierta"),

    /**
     * Inscripción cerrada - No se aceptan más inscripciones.
     */
    INSCRIPCION_CERRADA("Inscripción Cerrada"),

    /**
     * En curso - El pozo está siendo jugado.
     */
    EN_CURSO("En Curso"),

    /**
     * Finalizado - El pozo ha terminado.
     */
    FINALIZADO("Finalizado"),

    /**
     * Cancelado - El pozo fue cancelado.
     */
    CANCELADO("Cancelado"),

    /**
     * Suspendido - El pozo está temporalmente suspendido.
     */
    SUSPENDIDO("Suspendido");

    private final String displayName;

    EstadoPozo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
