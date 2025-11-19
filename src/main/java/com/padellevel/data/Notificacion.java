package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación enviada a un jugador.
 * Gestiona los diferentes tipos de notificaciones del sistema y su estado de lectura.
 */
@Entity
@Table(name = "notificacion")
public class Notificacion extends AbstractEntity {

    /**
     * Usuario destinatario de la notificación.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;

    /**
     * Título de la notificación.
     */
    @NotBlank
    @Column(nullable = false)
    private String titulo;

    /**
     * Mensaje o contenido de la notificación.
     */
    @NotBlank
    @Column(nullable = false, length = 2000)
    private String mensaje;

    /**
     * Tipo de notificación.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    /**
     * Canal por el que se envió la notificación.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotificacion canal;

    /**
     * Indica si la notificación ha sido leída.
     */
    @Column(nullable = false)
    private Boolean leida = false;

    /**
     * Fecha y hora en que se envió la notificación.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    /**
     * Fecha y hora en que se leyó la notificación.
     */
    private LocalDateTime fechaLeida;

    /**
     * Enfrentamiento relacionado (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "enfrentamiento_id")
    private Enfrentamiento enfrentamiento;

    /**
     * Torneo relacionado (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    /**
     * Club relacionado con la notificación (opcional).
     * Permite filtrar notificaciones específicas de un club.
     */
    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    // Constructores
    public Notificacion() {}

    // Getters y Setters
    public User getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(User destinatario) {
        this.destinatario = destinatario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacion canal) {
        this.canal = canal;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaLeida() {
        return fechaLeida;
    }

    public void setFechaLeida(LocalDateTime fechaLeida) {
        this.fechaLeida = fechaLeida;
    }

    public Enfrentamiento getEnfrentamiento() {
        return enfrentamiento;
    }

    public void setEnfrentamiento(Enfrentamiento enfrentamiento) {
        this.enfrentamiento = enfrentamiento;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }
}
