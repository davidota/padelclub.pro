package com.padellevel.data;

import jakarta.persistence.*;
import java.util.Objects;

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


