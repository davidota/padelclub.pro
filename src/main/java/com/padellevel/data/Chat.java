package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un chat o conversación.
 * Puede ser privado, de equipo, de torneo o de club.
 */
@Entity
@Table(name = "chat")
public class Chat extends AbstractEntity {

    /**
     * Nombre del chat (opcional para chats privados).
     */
    @Column(length = 200)
    private String nombre;

    /**
     * Tipo de chat.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoChat tipo;

    /**
     * Torneo relacionado (si es chat de torneo).
     */
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    /**
     * Equipo relacionado (si es chat de equipo).
     */
    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    /**
     * Club relacionado (si es chat de club).
     */
    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    /**
     * Participantes del chat.
     */
    @ManyToMany
    @JoinTable(
        name = "chat_participante",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participantes = new ArrayList<>();

    /**
     * Mensajes del chat.
     */
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMensaje> mensajes = new ArrayList<>();

    /**
     * Fecha de creación del chat.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha del último mensaje.
     */
    @Column(name = "ultimo_mensaje")
    private LocalDateTime ultimoMensaje;

    /**
     * Indica si el chat está activo.
     */
    private Boolean activo = true;

    // Constructor
    public Chat() {
        this.fechaCreacion = LocalDateTime.now();
        this.ultimoMensaje = LocalDateTime.now();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoChat getTipo() {
        return tipo;
    }

    public void setTipo(TipoChat tipo) {
        this.tipo = tipo;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public List<User> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<User> participantes) {
        this.participantes = participantes;
    }

    public List<ChatMensaje> getMensajes() {
        return mensajes;
    }

    public void setMensajes(List<ChatMensaje> mensajes) {
        this.mensajes = mensajes;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(LocalDateTime ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    /**
     * Verifica si un usuario es participante del chat.
     */
    public boolean esParticipante(User user) {
        return participantes != null && participantes.contains(user);
    }

    /**
     * Agrega un participante al chat.
     */
    public void agregarParticipante(User user) {
        if (!esParticipante(user)) {
            participantes.add(user);
        }
    }

    /**
     * Genera un nombre para el chat si no tiene uno.
     */
    public String getNombreDisplay() {
        if (nombre != null && !nombre.isEmpty()) {
            return nombre;
        }

        switch (tipo) {
            case TORNEO:
                return torneo != null ? "Chat de " + torneo.getNombre() : "Chat de Torneo";
            case EQUIPO:
                return equipo != null ? "Chat de Equipo" : "Chat de Equipo";
            case CLUB:
                return club != null ? "Chat de " + club.getNombre() : "Chat de Club";
            case PRIVADO:
                return "Chat Privado";
            default:
                return "Chat";
        }
    }
}
