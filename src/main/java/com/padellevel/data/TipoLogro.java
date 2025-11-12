package com.padellevel.data;

/**
 * Tipos de logros (achievements) que los jugadores pueden desbloquear.
 */
public enum TipoLogro {
    /**
     * Partidos jugados - Logros basados en cantidad de partidos.
     */
    PARTIDOS_JUGADOS("Partidos Jugados", "🎾"),

    /**
     * Victorias - Logros basados en victorias acumuladas.
     */
    VICTORIAS("Victorias", "🏆"),

    /**
     * Racha de victorias - Victoria consecutivas.
     */
    RACHA_VICTORIAS("Racha de Victorias", "🔥"),

    /**
     * Torneos ganados - Torneos donde se obtuvo el primer lugar.
     */
    TORNEOS_GANADOS("Torneos Ganados", "👑"),

    /**
     * Primera victoria - Primer partido ganado.
     */
    PRIMERA_VICTORIA("Primera Victoria", "🌟"),

    /**
     * Primera medalla - Primera vez en el podio.
     */
    PRIMERA_MEDALLA("Primera Medalla", "🥇"),

    /**
     * Veterano - Jugador con mucha experiencia.
     */
    VETERANO("Veterano", "👴"),

    /**
     * Invicto - Temporada sin derrotas.
     */
    INVICTO("Invicto", "💪"),

    /**
     * Colaborador - Participación activa en múltiples torneos.
     */
    COLABORADOR("Colaborador", "🤝"),

    /**
     * Remontador - Victorias tras ir perdiendo.
     */
    REMONTADOR("Remontador", "↗️"),

    /**
     * Goleador - Muchos puntos marcados.
     */
    GOLEADOR("Goleador", "⚡"),

    /**
     * Fair Play - Buen comportamiento deportivo.
     */
    FAIR_PLAY("Fair Play", "🤝"),

    /**
     * Debut - Primera participación en un torneo.
     */
    DEBUT("Debut", "🎬"),

    /**
     * Especialista - Dominio de una modalidad específica.
     */
    ESPECIALISTA("Especialista", "🎯");

    private final String displayName;
    private final String icon;

    TipoLogro(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
