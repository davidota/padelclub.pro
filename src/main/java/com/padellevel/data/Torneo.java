package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Torneo de Padel.
 * Un torneo puede contener múltiples pozos (pools) y tiene información
 * sobre inscripciones, premios, fechas, y configuración general.
 */
@Entity
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private TipoTorneo tipo;

    @ManyToMany
    private List<User> jugadores = new ArrayList<>();

    private int numeroEnfrentamientos;
    private int juegosPorEnfrentamiento;

    private int numeroEnfrentamientosSimultaneos = 1; // Updated default value

    @Enumerated(EnumType.STRING)
    private EstadoTorneo estado = EstadoTorneo.ABIERTO; // New field

    private int numeroDeVueltas; // Changed from Integer

    // New fields for enhanced tournament management

    /**
     * Fecha de inicio del torneo
     */
    private LocalDate fechaInicio;

    /**
     * Fecha de finalización del torneo
     */
    private LocalDate fechaFin;

    /**
     * Fecha y hora de inicio de inscripción
     */
    private LocalDateTime fechaInicioInscripcion;

    /**
     * Fecha y hora de cierre de inscripción
     */
    private LocalDateTime fechaCierreInscripcion;

    /**
     * Cupo máximo de equipos que pueden participar
     */
    @PositiveOrZero(message = "El cupo máximo debe ser mayor o igual a cero")
    private Integer cupoMaximo;

    /**
     * Indica si el torneo es gratuito
     */
    @Column(nullable = false)
    private Boolean esGratuito = true;

    /**
     * Precio de inscripción (si no es gratuito)
     */
    @PositiveOrZero(message = "El precio de inscripción debe ser mayor o igual a cero")
    private BigDecimal precioInscripcion;

    /**
     * Ubicación física del torneo
     */
    private String ubicacion;

    /**
     * URL de la imagen del torneo
     */
    private String imagenUrl;

    /**
     * Indica si el torneo es público (visible para todos)
     */
    @Column(nullable = false)
    private Boolean esPublico = true;

    /**
     * Indica si se permiten espectadores
     */
    @Column(nullable = false)
    private Boolean permitirEspectadores = true;

    // Relationships

    /**
     * Club organizador del torneo
     */
    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    /**
     * Usuario organizador del torneo
     */
    @ManyToOne
    @JoinColumn(name = "organizador_id")
    private User organizador;

    /**
     * Lista de inscripciones al torneo
     */
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    /**
     * Lista de premios del torneo
     */
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Premio> premios = new ArrayList<>();

    // Remove the duplicate @ManyToMany annotation if present
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enfrentamiento> enfrentamientos = new ArrayList<>();

    // New relationship: a torneo has multiple pozos
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pozo> pozos = new ArrayList<>();

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

    public List<Pozo> getPozos() {
        return pozos;
    }

    public void setPozos(List<Pozo> pozos) {
        this.pozos = pozos;
    }

    // Getters and setters for new fields

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Boolean getEsPublico() {
        return esPublico;
    }

    public void setEsPublico(Boolean esPublico) {
        this.esPublico = esPublico;
    }

    public Boolean getPermitirEspectadores() {
        return permitirEspectadores;
    }

    public void setPermitirEspectadores(Boolean permitirEspectadores) {
        this.permitirEspectadores = permitirEspectadores;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public User getOrganizador() {
        return organizador;
    }

    public void setOrganizador(User organizador) {
        this.organizador = organizador;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public List<Premio> getPremios() {
        return premios;
    }

    public void setPremios(List<Premio> premios) {
        this.premios = premios;
    }
}