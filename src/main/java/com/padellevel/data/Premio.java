package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad que representa un premio otorgado en un torneo o pozo.
 * Gestiona los premios individuales y de equipo, incluyendo podios y menciones especiales.
 */
@Entity
@Table(name = "premio")
public class Premio extends AbstractEntity {

    /**
     * Torneo al que pertenece el premio (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;

    /**
     * Pozo al que pertenece el premio (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "pozo_id")
    private Pozo pozo;

    /**
     * Tipo de premio.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPremio tipo;

    /**
     * Nombre del premio.
     */
    @NotBlank
    @Column(nullable = false)
    private String nombre;

    /**
     * Descripción del premio.
     */
    @Column(length = 1000)
    private String descripcion;

    /**
     * Equipo ganador del premio (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "equipo_ganador_id")
    private Equipo equipoGanador;

    /**
     * Jugador ganador del premio individual (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "jugador_ganador_id")
    private User jugadorGanador;

    /**
     * Posición en el ranking (1 = primer lugar, 2 = segundo lugar, etc.).
     */
    private Integer posicion;

    // Constructores
    public Premio() {}

    // Getters y Setters
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

    public TipoPremio getTipo() {
        return tipo;
    }

    public void setTipo(TipoPremio tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Equipo getEquipoGanador() {
        return equipoGanador;
    }

    public void setEquipoGanador(Equipo equipoGanador) {
        this.equipoGanador = equipoGanador;
    }

    public User getJugadorGanador() {
        return jugadorGanador;
    }

    public void setJugadorGanador(User jugadorGanador) {
        this.jugadorGanador = jugadorGanador;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
