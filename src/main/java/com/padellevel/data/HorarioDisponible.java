package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa un horario disponible para una pista.
 * Gestiona los slots de tiempo disponibles y su asignación a enfrentamientos.
 */
@Entity
@Table(name = "horario_disponible")
public class HorarioDisponible extends AbstractEntity {

    /**
     * Pista a la que pertenece este horario.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "pista_id", nullable = false)
    private Pista pista;

    /**
     * Fecha del horario disponible.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Hora de inicio del slot.
     */
    @NotNull
    @Column(nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora de fin del slot.
     */
    @NotNull
    @Column(nullable = false)
    private LocalTime horaFin;

    /**
     * Enfrentamiento asignado a este horario (si existe).
     */
    @ManyToOne
    @JoinColumn(name = "enfrentamiento_id")
    private Enfrentamiento enfrentamiento;

    /**
     * Indica si el horario está disponible para reservas.
     */
    @Column(nullable = false)
    private Boolean disponible = true;

    // Constructores
    public HorarioDisponible() {}

    // Getters y Setters
    public Pista getPista() {
        return pista;
    }

    public void setPista(Pista pista) {
        this.pista = pista;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Enfrentamiento getEnfrentamiento() {
        return enfrentamiento;
    }

    public void setEnfrentamiento(Enfrentamiento enfrentamiento) {
        this.enfrentamiento = enfrentamiento;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
}
