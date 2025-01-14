package com.padellevel.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoTorneo tipo;

    @ManyToMany
    private List<User> jugadores = new ArrayList<>();

    private int numeroEnfrentamientos;
    private int juegosPorEnfrentamiento;
    private String nombre;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoTorneo getTipo() {
        return tipo;
    }

    public void setTipo(TipoTorneo tipo) {
        this.tipo = tipo;
    }

    public List<User> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<User> jugadores) {
        this.jugadores = jugadores;
    }

    public int getNumeroEnfrentamientos() {
        return numeroEnfrentamientos;
    }

    public void setNumeroEnfrentamientos(int numeroEnfrentamientos) {
        this.numeroEnfrentamientos = numeroEnfrentamientos;
    }

    public int getJuegosPorEnfrentamiento() {
        return juegosPorEnfrentamiento;
    }

    public void setJuegosPorEnfrentamiento(int juegosPorEnfrentamiento) {
        this.juegosPorEnfrentamiento = juegosPorEnfrentamiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}