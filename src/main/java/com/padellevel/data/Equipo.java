package com.padellevel.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un Equipo de dos jugadores en un torneo.
 * Un equipo puede ser permanente o temporal (en el caso de Americano).
 */
@Entity
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String NombreEquipo = new String();

    @ManyToOne(optional = false)
    private User participante1;

    @ManyToOne(optional = false)
    private User participante2;

    // New mapping to link Equipo to Pozo
    @ManyToOne
    @JoinColumn(name = "pozo_id")
    private Pozo pozo;

    /**
     * Torneo al que pertenece el equipo
     */
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    /**
     * Indica si el equipo es temporal para modalidad Americano
     * En Americano, los jugadores rotan de pareja cada ronda
     */
    @Column(nullable = false)
    private Boolean esTemporalAmericano = false;

    /**
     * Número de ronda en la que se forma este equipo (para Americano)
     */
    private Integer rondaAmericano;

    /**
     * Estadísticas del equipo en diferentes enfrentamientos
     */
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EstadisticaEquipo> estadisticas = new ArrayList<>();

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getParticipante1() {
        return participante1;
    }

    public void setParticipante1(User participante1) {
        this.participante1 = participante1;
    }

    public User getParticipante2() {
        return participante2;
    }

    public void setParticipante2(User participante2) {
        this.participante2 = participante2;
    }

    // Getters and setters for new field
    public Pozo getPozo() {
        return pozo;
    }

    public void setPozo(Pozo pozo) {
        this.pozo = pozo;
    }

    // Método para obtener el nombre del equipo
    public String getNombreEquipo() {
        return NombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.NombreEquipo = nombreEquipo;
    }

    // Getters and setters for new fields

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Boolean getEsTemporalAmericano() {
        return esTemporalAmericano;
    }

    public void setEsTemporalAmericano(Boolean esTemporalAmericano) {
        this.esTemporalAmericano = esTemporalAmericano;
    }

    public Integer getRondaAmericano() {
        return rondaAmericano;
    }

    public void setRondaAmericano(Integer rondaAmericano) {
        this.rondaAmericano = rondaAmericano;
    }

    public List<EstadisticaEquipo> getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(List<EstadisticaEquipo> estadisticas) {
        this.estadisticas = estadisticas;
    }

    // Override equals and hashCode for entity comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Equipo equipo = (Equipo) o;
        return Objects.equals(id, equipo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


