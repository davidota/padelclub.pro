package com.padellevel.data;

/**
 * Tipos de pistas de pádel.
 */
public enum TipoPista {
    /**
     * Indoor - Pista cubierta, protegida de las inclemencias del tiempo.
     */
    INDOOR("Indoor", "🏢"),

    /**
     * Outdoor - Pista al aire libre.
     */
    OUTDOOR("Outdoor", "☀️"),

    /**
     * Cristal - Pista con paredes de cristal (panorámica).
     */
    CRISTAL("Cristal", "🔲"),

    /**
     * Muro - Pista con paredes de hormigón tradicionales.
     */
    MURO("Muro", "🧱"),

    /**
     * Panorámica - Pista con vistas panorámicas (generalmente en altura).
     */
    PANORAMICA("Panorámica", "🌄"),

    /**
     * Semi-cubierta - Pista parcialmente cubierta.
     */
    SEMI_CUBIERTA("Semi-cubierta", "🏛️"),

    /**
     * Profesional - Pista de nivel profesional con especificaciones oficiales.
     */
    PROFESIONAL("Profesional", "⭐");

    private final String displayName;
    private final String icon;

    TipoPista(String displayName, String icon) {
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
