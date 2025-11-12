package com.padellevel.data;

/**
 * Tipos de premios que se pueden otorgar.
 */
public enum TipoPremio {
    /**
     * Oro - Primer lugar.
     */
    ORO("Oro", "🥇", 1),

    /**
     * Plata - Segundo lugar.
     */
    PLATA("Plata", "🥈", 2),

    /**
     * Bronce - Tercer lugar.
     */
    BRONCE("Bronce", "🥉", 3),

    /**
     * Fair Play - Premio al juego limpio.
     */
    FAIR_PLAY("Fair Play", "🤝", null),

    /**
     * MVP - Jugador más valioso.
     */
    MVP("MVP", "⭐", null),

    /**
     * Mejor Pareja - Mejor dupla del torneo.
     */
    MEJOR_PAREJA("Mejor Pareja", "👥", null),

    /**
     * Goleador - Jugador con más puntos anotados.
     */
    GOLEADOR("Goleador", "⚡", null),

    /**
     * Revelación - Mejor jugador novel.
     */
    REVELACION("Revelación", "🌟", null),

    /**
     * Veterano - Mejor jugador veterano.
     */
    VETERANO("Veterano", "👴", null),

    /**
     * Participación - Premio por participación.
     */
    PARTICIPACION("Participación", "🎖️", null);

    private final String displayName;
    private final String icon;
    private final Integer posicion;

    TipoPremio(String displayName, String icon, Integer posicion) {
        this.displayName = displayName;
        this.icon = icon;
        this.posicion = posicion;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public boolean isPodium() {
        return posicion != null && posicion <= 3;
    }
}
