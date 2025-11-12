package com.padellevel.data;

/**
 * Niveles de habilidad de los jugadores.
 */
public enum NivelJugador {
    /**
     * Principiante - Jugador que está empezando.
     */
    PRINCIPIANTE("Principiante", "🌱", 1),

    /**
     * Iniciado - Jugador con conocimientos básicos.
     */
    INICIADO("Iniciado", "📚", 2),

    /**
     * Intermedio - Jugador con experiencia media.
     */
    INTERMEDIO("Intermedio", "🎯", 3),

    /**
     * Intermedio-Avanzado - Transición a nivel avanzado.
     */
    INTERMEDIO_AVANZADO("Intermedio-Avanzado", "🚀", 4),

    /**
     * Avanzado - Jugador experimentado.
     */
    AVANZADO("Avanzado", "⭐", 5),

    /**
     * Experto - Jugador de alto nivel.
     */
    EXPERTO("Experto", "💎", 6),

    /**
     * Profesional - Jugador de nivel profesional.
     */
    PROFESIONAL("Profesional", "👑", 7);

    private final String displayName;
    private final String icon;
    private final int orden;

    NivelJugador(String displayName, String icon, int orden) {
        this.displayName = displayName;
        this.icon = icon;
        this.orden = orden;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public int getOrden() {
        return orden;
    }
}
