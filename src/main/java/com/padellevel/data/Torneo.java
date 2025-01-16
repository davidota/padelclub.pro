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

    private String nombre;

    private int numeroEnfrentamientos;
    private int juegosPorEnfrentamiento;

    private int numeroEnfrentamientosSimultaneos = 1; // Updated default value

    @Enumerated(EnumType.STRING)
    private EstadoTorneo estado = EstadoTorneo.ABIERTO; // New field

    private int numeroDeVueltas; // Changed from Integer

    // Remove the duplicate @ManyToMany annotation if present
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enfrentamiento> enfrentamientos = new ArrayList<>();

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

    public int getNumeroEnfrentamientosSimultaneos() {
        return numeroEnfrentamientosSimultaneos;
    }

    public void setNumeroEnfrentamientosSimultaneos(int numeroEnfrentamientosSimultaneos) {
        this.numeroEnfrentamientosSimultaneos = numeroEnfrentamientosSimultaneos;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public void setEstado(EstadoTorneo estado) {
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumeroDeVueltas() {
        return numeroDeVueltas;
    }

    public void setNumeroDeVueltas(int numeroDeVueltas) {
        this.numeroDeVueltas = numeroDeVueltas;
    }

    public List<Enfrentamiento> getEnfrentamientos() {
        return enfrentamientos;
    }

    public void setEnfrentamientos(List<Enfrentamiento> enfrentamientos) {
        this.enfrentamientos = enfrentamientos;
    }
}