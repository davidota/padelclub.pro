package com.padellevel.data;

/**
 * Estados posibles de una inscripción.
 */
public enum EstadoInscripcion {
    /**
     * Pendiente - La inscripción está pendiente de confirmación.
     */
    PENDIENTE("Pendiente"),

    /**
     * Confirmada - La inscripción ha sido confirmada pero aún no está pagada (si aplica).
     */
    CONFIRMADA("Confirmada"),

    /**
     * Pagada - La inscripción ha sido confirmada y pagada.
     */
    PAGADA("Pagada"),

    /**
     * Cancelada - La inscripción fue cancelada por el jugador o administrador.
     */
    CANCELADA("Cancelada"),

    /**
     * Rechazada - La inscripción fue rechazada (por ejemplo, cupo lleno).
     */
    RECHAZADA("Rechazada"),

    /**
     * En lista de espera - El jugador está en lista de espera.
     */
    EN_LISTA_ESPERA("En Lista de Espera");

    private final String displayName;

    EstadoInscripcion(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
