package com.padellevel.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Configuración específica de cada club.
 * Se almacena como un objeto embebido en la entidad Club.
 */
@Embeddable
public class ConfiguracionClub {

    /**
     * Moneda utilizada por el club (USD, EUR, MXN, etc.)
     */
    @Column(name = "moneda")
    private String moneda = "USD";

    /**
     * Zona horaria del club
     */
    @Column(name = "zona_horaria")
    private String zonaHoraria = "UTC";

    /**
     * Idioma predeterminado del club
     */
    @Column(name = "idioma")
    private String idioma = "es";

    /**
     * Permite inscripciones públicas sin aprobación
     */
    @Column(name = "inscripciones_publicas")
    private Boolean inscripcionesPublicas = true;

    /**
     * Requiere aprobación del administrador para nuevos miembros
     */
    @Column(name = "requiere_aprobacion_miembros")
    private Boolean requiereAprobacionMiembros = false;

    /**
     * Tarifa por defecto para torneos de pago
     */
    @Column(name = "tarifa_torneo_default")
    private BigDecimal tarifaTorneoDefault;

    /**
     * Comisión del club sobre torneos de pago (porcentaje)
     */
    @Column(name = "comision_torneos")
    private BigDecimal comisionTorneos = BigDecimal.ZERO;

    /**
     * Número máximo de jugadores por torneo (0 = sin límite)
     */
    @Column(name = "max_jugadores_torneo")
    private Integer maxJugadoresTorneo = 0;

    /**
     * Permite a los miembros crear torneos
     */
    @Column(name = "miembros_pueden_crear_torneos")
    private Boolean miembrosPuedenCrearTorneos = false;

    /**
     * Activa el sistema de ranking interno del club
     */
    @Column(name = "ranking_activo")
    private Boolean rankingActivo = true;

    /**
     * Activa el sistema de gamificación
     */
    @Column(name = "gamificacion_activa")
    private Boolean gamificacionActiva = true;

    /**
     * Permite chats entre miembros
     */
    @Column(name = "chats_habilitados")
    private Boolean chatsHabilitados = true;

    /**
     * Email para notificaciones del club
     */
    @Column(name = "email_notificaciones")
    private String emailNotificaciones;

    /**
     * Mensaje de bienvenida para nuevos miembros
     */
    @Column(name = "mensaje_bienvenida", length = 1000)
    private String mensajeBienvenida;

    /**
     * Color primario del club (hex)
     */
    @Column(name = "color_primario")
    private String colorPrimario = "#1976d2";

    /**
     * Color secundario del club (hex)
     */
    @Column(name = "color_secundario")
    private String colorSecundario = "#dc004e";

    // Constructores
    public ConfiguracionClub() {}

    // Getters y Setters
    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public Boolean getInscripcionesPublicas() {
        return inscripcionesPublicas;
    }

    public void setInscripcionesPublicas(Boolean inscripcionesPublicas) {
        this.inscripcionesPublicas = inscripcionesPublicas;
    }

    public Boolean getRequiereAprobacionMiembros() {
        return requiereAprobacionMiembros;
    }

    public void setRequiereAprobacionMiembros(Boolean requiereAprobacionMiembros) {
        this.requiereAprobacionMiembros = requiereAprobacionMiembros;
    }

    public BigDecimal getTarifaTorneoDefault() {
        return tarifaTorneoDefault;
    }

    public void setTarifaTorneoDefault(BigDecimal tarifaTorneoDefault) {
        this.tarifaTorneoDefault = tarifaTorneoDefault;
    }

    public BigDecimal getComisionTorneos() {
        return comisionTorneos;
    }

    public void setComisionTorneos(BigDecimal comisionTorneos) {
        this.comisionTorneos = comisionTorneos;
    }

    public Integer getMaxJugadoresTorneo() {
        return maxJugadoresTorneo;
    }

    public void setMaxJugadoresTorneo(Integer maxJugadoresTorneo) {
        this.maxJugadoresTorneo = maxJugadoresTorneo;
    }

    public Boolean getMiembrosPuedenCrearTorneos() {
        return miembrosPuedenCrearTorneos;
    }

    public void setMiembrosPuedenCrearTorneos(Boolean miembrosPuedenCrearTorneos) {
        this.miembrosPuedenCrearTorneos = miembrosPuedenCrearTorneos;
    }

    public Boolean getRankingActivo() {
        return rankingActivo;
    }

    public void setRankingActivo(Boolean rankingActivo) {
        this.rankingActivo = rankingActivo;
    }

    public Boolean getGamificacionActiva() {
        return gamificacionActiva;
    }

    public void setGamificacionActiva(Boolean gamificacionActiva) {
        this.gamificacionActiva = gamificacionActiva;
    }

    public Boolean getChatsHabilitados() {
        return chatsHabilitados;
    }

    public void setChatsHabilitados(Boolean chatsHabilitados) {
        this.chatsHabilitados = chatsHabilitados;
    }

    public String getEmailNotificaciones() {
        return emailNotificaciones;
    }

    public void setEmailNotificaciones(String emailNotificaciones) {
        this.emailNotificaciones = emailNotificaciones;
    }

    public String getMensajeBienvenida() {
        return mensajeBienvenida;
    }

    public void setMensajeBienvenida(String mensajeBienvenida) {
        this.mensajeBienvenida = mensajeBienvenida;
    }

    public String getColorPrimario() {
        return colorPrimario;
    }

    public void setColorPrimario(String colorPrimario) {
        this.colorPrimario = colorPrimario;
    }

    public String getColorSecundario() {
        return colorSecundario;
    }

    public void setColorSecundario(String colorSecundario) {
        this.colorSecundario = colorSecundario;
    }
}
