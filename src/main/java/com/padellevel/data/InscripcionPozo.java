package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad que representa la inscripción de un jugador a un pozo específico.
 * Gestiona la participación de jugadores en pozos dentro de un torneo.
 */
@Entity
@Table(name = "inscripcion_pozo")
public class InscripcionPozo extends AbstractEntity {

    /**
     * Jugador que se inscribe al pozo.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private User jugador;

    /**
     * Pozo al que se inscribe.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "pozo_id", nullable = false)
    private Pozo pozo;

    /**
     * Inscripción al torneo asociada (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "inscripcion_torneo_id")
    private Inscripcion inscripcionTorneo;

    /**
     * Fecha y hora de la inscripción al pozo.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;

    /**
     * Estado de la inscripción al pozo.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado = EstadoInscripcion.PENDIENTE;

    /**
     * Equipo asignado al jugador en este pozo.
     */
    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    /**
     * Pago asociado a esta inscripción de pozo.
     */
    @OneToOne(mappedBy = "inscripcionPozo", cascade = CascadeType.ALL)
    private Pago pago;

    // Constructores
    public InscripcionPozo() {}

    // Getters y Setters
    public User getJugador() {
        return jugador;
    }

    public void setJugador(User jugador) {
        this.jugador = jugador;
    }

    public Pozo getPozo() {
        return pozo;
    }

    public void setPozo(Pozo pozo) {
        this.pozo = pozo;
    }

    public Inscripcion getInscripcionTorneo() {
        return inscripcionTorneo;
    }

    public void setInscripcionTorneo(Inscripcion inscripcionTorneo) {
        this.inscripcionTorneo = inscripcionTorneo;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
