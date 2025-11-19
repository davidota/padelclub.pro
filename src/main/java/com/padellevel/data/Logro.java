package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un logro o achievement del sistema de gamificación.
 * Los jugadores pueden desbloquear logros al alcanzar ciertas metas.
 */
@Entity
@Table(name = "logro")
public class Logro extends AbstractEntity {

    /**
     * Nombre del logro.
     */
    @NotBlank
    @Column(nullable = false)
    private String nombre;

    /**
     * Descripción del logro.
     */
    @NotBlank
    @Column(nullable = false, length = 1000)
    private String descripcion;

    /**
     * Icono del logro (puede ser una ruta, emoji o identificador).
     */
    private String icono;

    /**
     * Tipo de logro.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLogro tipo;

    /**
     * Meta numérica para desbloquear el logro (ej: 10 victorias, 5 torneos).
     */
    private Integer meta;

    /**
     * Club al que pertenece este logro.
     * Si es null, el logro es global (para todos los clubes).
     */
    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    /**
     * Jugadores que han desbloqueado este logro.
     */
    @ManyToMany
    @JoinTable(
        name = "logro_jugador",
        joinColumns = @JoinColumn(name = "logro_id"),
        inverseJoinColumns = @JoinColumn(name = "jugador_id")
    )
    private List<User> jugadores = new ArrayList<>();

    // Constructores
    public Logro() {}

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public TipoLogro getTipo() {
        return tipo;
    }

    public void setTipo(TipoLogro tipo) {
        this.tipo = tipo;
    }

    public Integer getMeta() {
        return meta;
    }

    public void setMeta(Integer meta) {
        this.meta = meta;
    }

    public List<User> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<User> jugadores) {
        this.jugadores = jugadores;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    /**
     * Verifica si el logro es global (no específico de un club).
     */
    public boolean esGlobal() {
        return club == null;
    }
}
