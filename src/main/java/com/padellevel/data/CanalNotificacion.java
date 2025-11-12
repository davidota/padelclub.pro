package com.padellevel.data;

/**
 * Canales por los cuales se pueden enviar notificaciones.
 */
public enum CanalNotificacion {
    /**
     * Email - Notificación por correo electrónico.
     */
    EMAIL("Email", "📧"),

    /**
     * Push - Notificación push en dispositivo móvil/web.
     */
    PUSH("Push", "🔔"),

    /**
     * SMS - Notificación por mensaje de texto.
     */
    SMS("SMS", "📱"),

    /**
     * In-app - Notificación dentro de la aplicación.
     */
    IN_APP("In-App", "📲"),

    /**
     * WhatsApp - Notificación via WhatsApp Business API.
     */
    WHATSAPP("WhatsApp", "💚");

    private final String displayName;
    private final String icon;

    CanalNotificacion(String displayName, String icon) {
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
