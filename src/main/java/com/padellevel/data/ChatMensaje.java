package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Entidad que representa un mensaje en un chat.
 */
@Entity
@Table(name = "chat_mensaje")
public class ChatMensaje extends AbstractEntity {

    /**
     * Chat al que pertenece el mensaje.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    /**
     * Usuario que envió el mensaje.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;

    /**
     * Contenido del mensaje.
     */
    @NotBlank
    @Column(nullable = false, length = 2000)
    private String contenido;

    /**
     * Fecha y hora del mensaje.
     */
    @NotNull
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Indica si el mensaje fue leído.
     */
    private Boolean leido = false;

    /**
     * Indica si el mensaje fue editado.
     */
    private Boolean editado = false;

    /**
     * Fecha de edición del mensaje.
     */
    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;

    // Constructor
    public ChatMensaje() {
        this.fechaHora = LocalDateTime.now();
    }

    public ChatMensaje(Chat chat, User remitente, String contenido) {
        this();
        this.chat = chat;
        this.remitente = remitente;
        this.contenido = contenido;
    }

    // Getters y Setters
    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getRemitente() {
        return remitente;
    }

    public void setRemitente(User remitente) {
        this.remitente = remitente;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Boolean getLeido() {
        return leido != null ? leido : false;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    public Boolean getEditado() {
        return editado != null ? editado : false;
    }

    public void setEditado(Boolean editado) {
        this.editado = editado;
    }

    public LocalDateTime getFechaEdicion() {
        return fechaEdicion;
    }

    public void setFechaEdicion(LocalDateTime fechaEdicion) {
        this.fechaEdicion = fechaEdicion;
    }

    /**
     * Edita el contenido del mensaje.
     */
    public void editar(String nuevoContenido) {
        this.contenido = nuevoContenido;
        this.editado = true;
        this.fechaEdicion = LocalDateTime.now();
    }
}
