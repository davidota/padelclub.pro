package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad que representa las estadísticas de un equipo en un torneo o pozo.
 * Rastrea el desempeño, resultados y posición del equipo en la competición.
 */
@Entity
@Table(name = "estadistica_equipo")
public class EstadisticaEquipo extends AbstractEntity {

    /**
     * Equipo al que pertenecen las estadísticas.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    /**
     * Torneo al que pertenecen las estadísticas (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    /**
     * Pozo al que pertenecen las estadísticas (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "pozo_id")
    private Pozo pozo;

    /**
     * Número de partidos jugados.
     */
    @Column(nullable = false)
    private Integer partidosJugados = 0;

    /**
     * Número de partidos ganados.
     */
    @Column(nullable = false)
    private Integer partidosGanados = 0;

    /**
     * Número de partidos perdidos.
     */
    @Column(nullable = false)
    private Integer partidosPerdidos = 0;

    /**
     * Número de sets ganados.
     */
    @Column(nullable = false)
    private Integer setsGanados = 0;

    /**
     * Número de sets perdidos.
     */
    @Column(nullable = false)
    private Integer setsPerdidos = 0;

    /**
     * Número de juegos ganados.
     */
    @Column(nullable = false)
    private Integer juegosGanados = 0;

    /**
     * Número de juegos perdidos.
     */
    @Column(nullable = false)
    private Integer juegosPerdidos = 0;

    /**
     * Puntos acumulados en el torneo/pozo.
     */
    @Column(nullable = false)
    private Integer puntos = 0;

    /**
     * Posición actual en la tabla de clasificación.
     */
    private Integer posicion;

    /**
     * Rendimiento general del equipo (métrica calculada).
     */
    private Double rendimiento = 0.0;

    // Constructores
    public EstadisticaEquipo() {}

    // Getters y Setters
    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Pozo getPozo() {
        return pozo;
    }

    public void setPozo(Pozo pozo) {
        this.pozo = pozo;
    }

    public Integer getPartidosJugados() {
        return partidosJugados;
    }

    public void setPartidosJugados(Integer partidosJugados) {
        this.partidosJugados = partidosJugados;
    }

    public Integer getPartidosGanados() {
        return partidosGanados;
    }

    public void setPartidosGanados(Integer partidosGanados) {
        this.partidosGanados = partidosGanados;
    }

    public Integer getPartidosPerdidos() {
        return partidosPerdidos;
    }

    public void setPartidosPerdidos(Integer partidosPerdidos) {
        this.partidosPerdidos = partidosPerdidos;
    }

    public Integer getSetsGanados() {
        return setsGanados;
    }

    public void setSetsGanados(Integer setsGanados) {
        this.setsGanados = setsGanados;
    }

    public Integer getSetsPerdidos() {
        return setsPerdidos;
    }

    public void setSetsPerdidos(Integer setsPerdidos) {
        this.setsPerdidos = setsPerdidos;
    }

    public Integer getJuegosGanados() {
        return juegosGanados;
    }

    public void setJuegosGanados(Integer juegosGanados) {
        this.juegosGanados = juegosGanados;
    }

    public Integer getJuegosPerdidos() {
        return juegosPerdidos;
    }

    public void setJuegosPerdidos(Integer juegosPerdidos) {
        this.juegosPerdidos = juegosPerdidos;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Double getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(Double rendimiento) {
        this.rendimiento = rendimiento;
    }
}
