package com.padellevel.data;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "enfrentamientos")
public class Enfrentamiento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "equipo1_id", nullable = false)
    private Equipo equipo1;

    @ManyToOne
    @JoinColumn(name = "equipo2_id", nullable = false)
    private Equipo equipo2;

    private String resultado;

    // Constructores
    public Enfrentamiento() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Equipo getEquipo1() {
        return equipo1;
    }

    public void setEquipo1(Equipo equipo1) {
        this.equipo1 = equipo1;
    }

    public Equipo getEquipo2() {
        return equipo2;
    }

    public void setEquipo2(Equipo equipo2) {
        this.equipo2 = equipo2;
    }

    public String getResultado() {
        return resultado;
    }

    public void setEquipoGanador(String resultado) {
        this.resultado = resultado;
    }
}