package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Pozo (Pool) dentro de un torneo.
 * Un pozo es un grupo o fase del torneo con una modalidad de juego específica.
 */
@Entity
public class Pozo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del pozo es obligatorio")
    @Column(nullable = false)
    private String nombre;

    private String tipoEnfrentamiento; // e.g., "Round-Robin", "Tie-Break", "Clásico"

    /**
     * Modalidad de juego del pozo
     */
    @Enumerated(EnumType.STRING)
    private ModalidadPozo modalidad;

    /**
     * Estado actual del pozo
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPozo estado = EstadoPozo.INSCRIPCION_ABIERTA;

    /**
     * Cupo máximo de equipos que pueden participar en el pozo
     */
    @PositiveOrZero(message = "El cupo máximo debe ser mayor o igual a cero")
    private Integer cupoMaximo;

    /**
     * Indica si el pozo es gratuito
     */
    @Column(nullable = false)
    private Boolean esGratuito = true;

    /**
     * Precio de inscripción al pozo (si no es gratuito)
     */
    @PositiveOrZero(message = "El precio de inscripción debe ser mayor o igual a cero")
    private BigDecimal precioInscripcion;

    /**
     * Fecha y hora de inicio de inscripción al pozo
     */
    private LocalDateTime fechaInicioInscripcion;

    /**
     * Fecha y hora de cierre de inscripción al pozo
     */
    private LocalDateTime fechaCierreInscripcion;

    /**
     * Fecha de inicio del pozo
     */
    private LocalDate fechaInicio;

    /**
     * Fecha de finalización del pozo
     */
    private LocalDate fechaFin;

    /**
     * Número de rondas para modalidad Americano
     */
    private Integer numeroDeRondas;

    /**
     * Indica si se permiten cambios de parejas en modalidad Americano
     */
    private Boolean permiteCambiosParejas;

    // Relationships

    @ManyToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @OneToMany(mappedBy = "pozo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enfrentamiento> enfrentamientos = new ArrayList<>();

    /**
     * Lista de inscripciones al pozo
     */
    @OneToMany(mappedBy = "pozo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InscripcionPozo> inscripciones = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoEnfrentamiento() { return tipoEnfrentamiento; }
    public void setTipoEnfrentamiento(String tipoEnfrentamiento) { this.tipoEnfrentamiento = tipoEnfrentamiento; }

    public Torneo getTorneo() { return torneo; }
    public void setTorneo(Torneo torneo) { this.torneo = torneo; }

    public List<Enfrentamiento> getEnfrentamientos() { return enfrentamientos; }
    public void setEnfrentamientos(List<Enfrentamiento> enfrentamientos) { this.enfrentamientos = enfrentamientos; }

    // Getters and setters for new fields

    public ModalidadPozo getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadPozo modalidad) {
        this.modalidad = modalidad;
    }

    public EstadoPozo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPozo estado) {
        this.estado = estado;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public Boolean getEsGratuito() {
        return esGratuito;
    }

    public void setEsGratuito(Boolean esGratuito) {
        this.esGratuito = esGratuito;
    }

    public BigDecimal getPrecioInscripcion() {
        return precioInscripcion;
    }

    public void setPrecioInscripcion(BigDecimal precioInscripcion) {
        this.precioInscripcion = precioInscripcion;
    }

    public LocalDateTime getFechaInicioInscripcion() {
        return fechaInicioInscripcion;
    }

    public void setFechaInicioInscripcion(LocalDateTime fechaInicioInscripcion) {
        this.fechaInicioInscripcion = fechaInicioInscripcion;
    }

    public LocalDateTime getFechaCierreInscripcion() {
        return fechaCierreInscripcion;
    }

    public void setFechaCierreInscripcion(LocalDateTime fechaCierreInscripcion) {
        this.fechaCierreInscripcion = fechaCierreInscripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getNumeroDeRondas() {
        return numeroDeRondas;
    }

    public void setNumeroDeRondas(Integer numeroDeRondas) {
        this.numeroDeRondas = numeroDeRondas;
    }

    public Boolean getPermiteCambiosParejas() {
        return permiteCambiosParejas;
    }

    public void setPermiteCambiosParejas(Boolean permiteCambiosParejas) {
        this.permiteCambiosParejas = permiteCambiosParejas;
    }

    public List<InscripcionPozo> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<InscripcionPozo> inscripciones) {
        this.inscripciones = inscripciones;
    }
}
