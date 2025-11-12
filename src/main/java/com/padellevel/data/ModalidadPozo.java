package com.padellevel.data;

/**
 * Modalidad de juego para un pozo (pool).
 */
public enum ModalidadPozo {
    /**
     * Modalidad Americana - Los jugadores rotan de pareja en cada ronda.
     * Se calcula el ranking individual, no por equipo.
     */
    AMERICANO("Americano", "Rotación de parejas, ranking individual"),

    /**
     * Todos contra Todos (Round Robin) - Cada equipo juega contra todos los demás.
     * Sistema de puntos: Victoria = 3, Empate = 1, Derrota = 0
     */
    TODOS_CONTRA_TODOS("Todos contra Todos", "Cada equipo juega contra todos"),

    /**
     * Eliminación Directa - Sistema de bracket con eliminación.
     * Los perdedores quedan eliminados del torneo.
     */
    ELIMINACION_DIRECTA("Eliminación Directa", "Sistema de bracket"),

    /**
     * Grupos + Eliminación - Primera fase por grupos, luego eliminación directa.
     */
    GRUPOS_ELIMINACION("Grupos + Eliminación", "Fase de grupos seguida de eliminación");

    private final String displayName;
    private final String description;

    ModalidadPozo(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
