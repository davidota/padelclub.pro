package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa la inscripción de un jugador a un torneo.
 * Gestiona el registro de jugadores, parejas y disponibilidad de fechas.
 */
@Entity
@Table(name = "inscripcion")
public class Inscripcion extends AbstractEntity {

    /**
     * Jugador que se inscribe.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private User jugador;

    /**
     * Torneo al que se inscribe.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    /**
     * Fecha y hora de la inscripción.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;

    /**
     * Estado de la inscripción.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado = EstadoInscripcion.PENDIENTE;

    /**
     * Pareja del jugador (opcional, para torneos de dobles).
     */
    @ManyToOne
    @JoinColumn(name = "pareja_id")
    private User pareja;

    /**
     * Fechas en las que el jugador está disponible para jugar.
     */
    @ElementCollection
    @CollectionTable(name = "inscripcion_fechas_disponibles",
                     joinColumns = @JoinColumn(name = "inscripcion_id"))
    @Column(name = "fecha")
    private List<LocalDate> fechasDisponibles = new ArrayList<>();

    /**
     * Pago asociado a esta inscripción.
     */
    @OneToOne(mappedBy = "inscripcion", cascade = CascadeType.ALL)
    private Pago pago;

    /**
     * Comentarios o notas adicionales del jugador.
     */
    @Column(length = 1000)
    private String comentarios;

    // Constructores
    public Inscripcion() {}

    // Getters y Setters
    public User getJugador() {
        return jugador;
    }

    public void setJugador(User jugador) {
        this.jugador = jugador;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
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

    public User getPareja() {
        return pareja;
    }

    public void setPareja(User pareja) {
        this.pareja = pareja;
    }

    public List<LocalDate> getFechasDisponibles() {
        return fechasDisponibles;
    }

    public void setFechasDisponibles(List<LocalDate> fechasDisponibles) {
        this.fechasDisponibles = fechasDisponibles;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
