package com.padellevel.data;

/**
 * Tipos de chat disponibles en el sistema.
 */
public enum TipoChat {
    /**
     * Chat privado entre dos usuarios.
     */
    PRIVADO("Privado"),

    /**
     * Chat de grupo para un equipo.
     */
    EQUIPO("Equipo"),

    /**
     * Chat público de un torneo.
     */
    TORNEO("Torneo"),

    /**
     * Chat general del club.
     */
    CLUB("Club");

    private final String displayName;

    TipoChat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
