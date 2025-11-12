package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una pista de pádel en un club.
 * Gestiona la información de las pistas y su disponibilidad.
 */
@Entity
@Table(name = "pista")
public class Pista extends AbstractEntity {

    /**
     * Nombre descriptivo de la pista.
     */
    @NotBlank
    @Column(nullable = false)
    private String nombre;

    /**
     * Número de la pista dentro del club.
     */
    @NotNull
    @Column(nullable = false)
    private Integer numero;

    /**
     * Club al que pertenece esta pista.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    /**
     * Tipo de pista (Indoor, Outdoor, etc.).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPista tipo;

    /**
     * Indica si la pista está disponible para reservas.
     */
    @Column(nullable = false)
    private Boolean disponible = true;

    /**
     * Horarios disponibles para esta pista.
     */
    @OneToMany(mappedBy = "pista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HorarioDisponible> horarios = new ArrayList<>();

    // Constructores
    public Pista() {}

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public TipoPista getTipo() {
        return tipo;
    }

    public void setTipo(TipoPista tipo) {
        this.tipo = tipo;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public List<HorarioDisponible> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioDisponible> horarios) {
        this.horarios = horarios;
    }
}
