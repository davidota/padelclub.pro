package com.padellevel.data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Enfrentamiento (partido) entre dos equipos.
 * Contiene información detallada sobre el resultado, sets, fecha, pista, árbitro, etc.
 */
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
    @JoinColumn(name = "pozo_id")
    private Pozo pozo;

    // Change nullable flag to true for generated records that do not set teams.
    @ManyToOne
    @JoinColumn(name = "equipo1_id", nullable = true)
    private Equipo equipo1;

    @ManyToOne
    @JoinColumn(name = "equipo2_id", nullable = true)
    private Equipo equipo2;

    /**
     * @deprecated Use sets field instead for detailed set information
     */
    @Deprecated
    private String resultado;

    /**
     * @deprecated Use sets field (List<SetEntity>) instead for structured set data
     */
    @Deprecated
    @ElementCollection
    private List<String> juegos;

    // New fields for enhanced match management

    /**
     * Número de ronda en el torneo/pozo
     */
    private Integer ronda;

    /**
     * Número de pista/cancha donde se juega el partido
     */
    private Integer pista;

    /**
     * Fecha y hora programada para el partido
     */
    private LocalDateTime fechaProgramada;

    /**
     * Fecha y hora real en que se jugó el partido
     */
    private LocalDateTime fechaJugado;

    /**
     * Estado actual del enfrentamiento
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEnfrentamiento estado = EstadoEnfrentamiento.PENDIENTE_PROGRAMACION;

    /**
     * Número de sets ganados por el equipo 1
     */
    private Integer setsEquipo1;

    /**
     * Número de sets ganados por el equipo 2
     */
    private Integer setsEquipo2;

    /**
     * Lista detallada de sets jugados en el partido
     */
    @ElementCollection
    @CollectionTable(name = "enfrentamiento_sets", joinColumns = @JoinColumn(name = "enfrentamiento_id"))
    private List<SetEntity> sets = new ArrayList<>();

    /**
     * Usuario árbitro del partido (opcional)
     */
    @ManyToOne
    @JoinColumn(name = "arbitro_id")
    private User arbitro;

    /**
     * Observaciones o comentarios sobre el partido
     */
    @Column(length = 1000)
    private String observaciones;

    /**
     * Número de fase en eliminación directa (32, 16, 8, 4, 2, 1)
     * Ej: 16 = Octavos de final, 8 = Cuartos, 4 = Semifinal, 2 = Final
     */
    private Integer numeroFase;

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

    public Pozo getPozo() {
        return pozo;
    }

    public void setPozo(Pozo pozo) {
        this.pozo = pozo;
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

    /**
     * @deprecated Use sets field instead
     */
    @Deprecated
    public String getResultado() {
        return resultado;
    }

    /**
     * @deprecated Use sets field instead
     */
    @Deprecated
    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    /**
     * @deprecated Use sets field instead
     */
    @Deprecated
    public List<String> getJuegos() {
        return juegos;
    }

    /**
     * @deprecated Use sets field instead
     */
    @Deprecated
    public void setJuegos(List<String> juegos) {
        this.juegos = juegos;
    }

    // Getters and setters for new fields

    public Integer getRonda() {
        return ronda;
    }

    public void setRonda(Integer ronda) {
        this.ronda = ronda;
    }

    public Integer getPista() {
        return pista;
    }

    public void setPista(Integer pista) {
        this.pista = pista;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public LocalDateTime getFechaJugado() {
        return fechaJugado;
    }

    public void setFechaJugado(LocalDateTime fechaJugado) {
        this.fechaJugado = fechaJugado;
    }

    public EstadoEnfrentamiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnfrentamiento estado) {
        this.estado = estado;
    }

    public Integer getSetsEquipo1() {
        return setsEquipo1;
    }

    public void setSetsEquipo1(Integer setsEquipo1) {
        this.setsEquipo1 = setsEquipo1;
    }

    public Integer getSetsEquipo2() {
        return setsEquipo2;
    }

    public void setSetsEquipo2(Integer setsEquipo2) {
        this.setsEquipo2 = setsEquipo2;
    }

    public List<SetEntity> getSets() {
        return sets;
    }

    public void setSets(List<SetEntity> sets) {
        this.sets = sets;
    }

    public User getArbitro() {
        return arbitro;
    }

    public void setArbitro(User arbitro) {
        this.arbitro = arbitro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getNumeroFase() {
        return numeroFase;
    }

    public void setNumeroFase(Integer numeroFase) {
        this.numeroFase = numeroFase;
    }

    // Utility methods

    /**
     * Calcula y actualiza el número de sets ganados por cada equipo
     * basándose en la lista de sets jugados
     */
    public void calcularSetsGanados() {
        if (sets == null || sets.isEmpty()) {
            this.setsEquipo1 = 0;
            this.setsEquipo2 = 0;
            return;
        }

        int setsE1 = 0;
        int setsE2 = 0;

        for (SetEntity set : sets) {
            Integer ganador = set.getGanador();
            if (ganador != null) {
                if (ganador == 1) {
                    setsE1++;
                } else if (ganador == 2) {
                    setsE2++;
                }
            }
        }

        this.setsEquipo1 = setsE1;
        this.setsEquipo2 = setsE2;
    }

    /**
     * Determina el equipo ganador del enfrentamiento
     * @return 1 si ganó equipo1, 2 si ganó equipo2, null si no hay ganador o empate
     */
    public Integer getGanador() {
        if (setsEquipo1 == null || setsEquipo2 == null) {
            return null;
        }
        if (setsEquipo1 > setsEquipo2) {
            return 1;
        } else if (setsEquipo2 > setsEquipo1) {
            return 2;
        }
        return null;
    }

    /**
     * Retorna una representación en string del resultado (ej: "6-4, 7-6(3)")
     */
    public String getResultadoFormateado() {
        if (sets == null || sets.isEmpty()) {
            return "-";
        }
        return sets.stream()
                .map(SetEntity::toString)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse("-");
    }
}