package com.padellevel.data;

/**
 * Estados posibles de un enfrentamiento (partido).
 */
public enum EstadoEnfrentamiento {
    /**
     * Programado - El partido está programado pero no ha comenzado.
     */
    PROGRAMADO("Programado"),

    /**
     * En curso - El partido está siendo jugado actualmente.
     */
    EN_CURSO("En Curso"),

    /**
     * Finalizado - El partido ha terminado con resultado.
     */
    FINALIZADO("Finalizado"),

    /**
     * Cancelado - El partido fue cancelado.
     */
    CANCELADO("Cancelado"),

    /**
     * Pospuesto - El partido fue pospuesto para otra fecha.
     */
    POSPUESTO("Pospuesto"),

    /**
     * Walkover - Uno de los equipos no se presentó.
     */
    WALKOVER("Walkover"),

    /**
     * Pendiente programación - El partido existe pero aún no tiene fecha/hora asignada.
     */
    PENDIENTE_PROGRAMACION("Pendiente Programación");

    private final String displayName;

    EstadoEnfrentamiento(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
