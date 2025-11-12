package com.padellevel.data;

/**
 * Métodos de pago soportados.
 */
public enum MetodoPago {
    /**
     * Stripe - Tarjeta de crédito/débito via Stripe.
     */
    STRIPE("Stripe", "Tarjeta de crédito/débito"),

    /**
     * PayPal - Pago via PayPal.
     */
    PAYPAL("PayPal", "PayPal"),

    /**
     * Efectivo - Pago en efectivo en el club.
     */
    EFECTIVO("Efectivo", "Pago en efectivo"),

    /**
     * Transferencia bancaria.
     */
    TRANSFERENCIA("Transferencia Bancaria", "Transferencia bancaria"),

    /**
     * Bizum - Sistema de pago móvil español.
     */
    BIZUM("Bizum", "Pago móvil instantáneo"),

    /**
     * Gratuito - Sin pago requerido.
     */
    GRATUITO("Gratuito", "Sin costo");

    private final String displayName;
    private final String description;

    MetodoPago(String displayName, String description) {
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
