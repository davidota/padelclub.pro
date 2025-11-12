package com.padellevel.data;

/**
 * Estados posibles de un pago.
 */
public enum EstadoPago {
    /**
     * Pendiente - El pago está pendiente de procesamiento.
     */
    PENDIENTE("Pendiente"),

    /**
     * Procesando - El pago está siendo procesado.
     */
    PROCESANDO("Procesando"),

    /**
     * Completado - El pago se completó exitosamente.
     */
    COMPLETADO("Completado"),

    /**
     * Fallido - El pago falló.
     */
    FALLIDO("Fallido"),

    /**
     * Reembolsado - El pago fue reembolsado.
     */
    REEMBOLSADO("Reembolsado"),

    /**
     * Cancelado - El pago fue cancelado antes de completarse.
     */
    CANCELADO("Cancelado"),

    /**
     * Parcialmente reembolsado - Solo una parte del pago fue reembolsada.
     */
    PARCIALMENTE_REEMBOLSADO("Parcialmente Reembolsado");

    private final String displayName;

    EstadoPago(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
