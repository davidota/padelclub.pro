package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidad que representa las estadísticas de un jugador en un torneo o pozo.
 * Rastrea el desempeño, resultados y métricas de un jugador.
 */
@Entity
@Table(name = "estadistica_jugador")
public class EstadisticaJugador extends AbstractEntity {

    /**
     * Jugador al que pertenecen las estadísticas.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private User jugador;

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
     * Número de partidos empatados.
     */
    @Column(nullable = false)
    private Integer partidosEmpatados = 0;

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
     * Puntos a favor.
     */
    @Column(nullable = false)
    private Integer puntosAFavor = 0;

    /**
     * Puntos en contra.
     */
    @Column(nullable = false)
    private Integer puntosEnContra = 0;

    /**
     * Porcentaje de victorias.
     */
    private Double porcentajeVictorias = 0.0;

    /**
     * Rendimiento general (métrica calculada).
     */
    private Double rendimiento = 0.0;

    /**
     * Racha actual (positiva = victorias consecutivas, negativa = derrotas consecutivas).
     */
    @Column(nullable = false)
    private Integer racha = 0;

    /**
     * Posición final en el torneo o pozo.
     */
    private Integer posicionFinal;

    /**
     * Premio obtenido (si aplica).
     */
    @Enumerated(EnumType.STRING)
    private TipoPremio premio;

    /**
     * Puntos acumulados en el torneo/pozo.
     */
    @Column(nullable = false)
    private Integer puntos = 0;

    /**
     * Fecha y hora de la última actualización de estadísticas.
     */
    private LocalDateTime ultimaActualizacion;

    // Constructores
    public EstadisticaJugador() {}

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

    public Integer getPartidosEmpatados() {
        return partidosEmpatados;
    }

    public void setPartidosEmpatados(Integer partidosEmpatados) {
        this.partidosEmpatados = partidosEmpatados;
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

    public Integer getPuntosAFavor() {
        return puntosAFavor;
    }

    public void setPuntosAFavor(Integer puntosAFavor) {
        this.puntosAFavor = puntosAFavor;
    }

    public Integer getPuntosEnContra() {
        return puntosEnContra;
    }

    public void setPuntosEnContra(Integer puntosEnContra) {
        this.puntosEnContra = puntosEnContra;
    }

    public Double getPorcentajeVictorias() {
        return porcentajeVictorias;
    }

    public void setPorcentajeVictorias(Double porcentajeVictorias) {
        this.porcentajeVictorias = porcentajeVictorias;
    }

    public Double getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(Double rendimiento) {
        this.rendimiento = rendimiento;
    }

    public Integer getRacha() {
        return racha;
    }

    public void setRacha(Integer racha) {
        this.racha = racha;
    }

    public Integer getPosicionFinal() {
        return posicionFinal;
    }

    public void setPosicionFinal(Integer posicionFinal) {
        this.posicionFinal = posicionFinal;
    }

    public TipoPremio getPremio() {
        return premio;
    }

    public void setPremio(TipoPremio premio) {
        this.premio = premio;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}
