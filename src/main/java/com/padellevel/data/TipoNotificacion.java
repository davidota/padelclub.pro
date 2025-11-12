package com.padellevel.data;

/**
 * Tipos de notificaciones del sistema.
 */
public enum TipoNotificacion {
    /**
     * Partido programado - Notificación de un nuevo partido programado.
     */
    PARTIDO_PROGRAMADO("Partido Programado", "📅"),

    /**
     * Resultado - Notificación de resultado de un partido.
     */
    RESULTADO("Resultado", "🎾"),

    /**
     * Cambio de calendario - Cambio en la programación de un partido.
     */
    CAMBIO_CALENDARIO("Cambio de Calendario", "🔄"),

    /**
     * Inscripción confirmada - Confirmación de inscripción exitosa.
     */
    INSCRIPCION_CONFIRMADA("Inscripción Confirmada", "✅"),

    /**
     * Pago exitoso - Confirmación de pago completado.
     */
    PAGO_EXITOSO("Pago Exitoso", "💳"),

    /**
     * Premio - Notificación de premio ganado.
     */
    PREMIO("Premio Ganado", "🏆"),

    /**
     * Recordatorio - Recordatorio general.
     */
    RECORDATORIO("Recordatorio", "⏰"),

    /**
     * Mensaje - Mensaje de otro usuario o administrador.
     */
    MENSAJE("Mensaje", "💬"),

    /**
     * Actualización de torneo - Cambios generales en el torneo.
     */
    ACTUALIZACION_TORNEO("Actualización de Torneo", "📢"),

    /**
     * Logro desbloqueado - Nuevo logro conseguido.
     */
    LOGRO_DESBLOQUEADO("Logro Desbloqueado", "🎖️"),

    /**
     * Sistema - Notificaciones del sistema.
     */
    SISTEMA("Sistema", "⚙️");

    private final String displayName;
    private final String icon;

    TipoNotificacion(String displayName, String icon) {
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
