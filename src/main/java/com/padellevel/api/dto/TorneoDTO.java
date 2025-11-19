package com.padellevel.api.dto;

import com.padellevel.data.EstadoTorneo;
import com.padellevel.data.Torneo;
import com.padellevel.data.TipoTorneo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para exponer información de torneos en la API REST.
 */
@Schema(description = "Información de un torneo")
public class TorneoDTO {

    @Schema(description = "ID del torneo", example = "1")
    private Long id;

    @Schema(description = "Nombre del torneo", example = "Torneo de Verano 2025")
    private String nombre;

    @Schema(description = "Descripción del torneo")
    private String descripcion;

    @Schema(description = "Tipo de torneo", example = "ROUND_ROBIN")
    private TipoTorneo tipo;

    @Schema(description = "Estado del torneo", example = "ACTIVO")
    private EstadoTorneo estado;

    @Schema(description = "Fecha de inicio")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de fin")
    private LocalDate fechaFin;

    @Schema(description = "Fecha de inicio de inscripciones")
    private LocalDateTime fechaInicioInscripcion;

    @Schema(description = "Fecha de cierre de inscripciones")
    private LocalDateTime fechaCierreInscripcion;

    @Schema(description = "Cupo máximo de jugadores", example = "32")
    private Integer cupoMaximo;

    @Schema(description = "Número actual de inscritos", example = "18")
    private Integer inscritosActuales;

    @Schema(description = "Precio de inscripción", example = "25.00")
    private BigDecimal precioInscripcion;

    @Schema(description = "Indica si el torneo es gratuito", example = "false")
    private Boolean esGratuito;

    @Schema(description = "ID del club organizador", example = "1")
    private Long clubId;

    @Schema(description = "Nombre del club organizador", example = "Club Padel Pro")
    private String clubNombre;

    // Constructor vacío
    public TorneoDTO() {}

    // Constructor desde entidad
    public TorneoDTO(Torneo torneo) {
        this.id = torneo.getId();
        this.nombre = torneo.getNombre();
        this.descripcion = torneo.getDescripcion();
        this.tipo = torneo.getTipo();
        this.estado = torneo.getEstado();
        this.fechaInicio = torneo.getFechaInicio();
        this.fechaFin = torneo.getFechaFin();
        this.fechaInicioInscripcion = torneo.getFechaInicioInscripcion();
        this.fechaCierreInscripcion = torneo.getFechaCierreInscripcion();
        this.cupoMaximo = torneo.getCupoMaximo();
        this.precioInscripcion = torneo.getPrecioInscripcion();
        this.esGratuito = torneo.getEsGratuito();
        if (torneo.getClub() != null) {
            this.clubId = torneo.getClub().getId();
            this.clubNombre = torneo.getClub().getNombre();
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TipoTorneo getTipo() {
        return tipo;
    }

    public void setTipo(TipoTorneo tipo) {
        this.tipo = tipo;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public void setEstado(EstadoTorneo estado) {
        this.estado = estado;
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

    public Integer getInscritosActuales() {
        return inscritosActuales;
    }

    public void setInscritosActuales(Integer inscritosActuales) {
        this.inscritosActuales = inscritosActuales;
    }

    public BigDecimal getPrecioInscripcion() {
        return precioInscripcion;
    }

    public void setPrecioInscripcion(BigDecimal precioInscripcion) {
        this.precioInscripcion = precioInscripcion;
    }

    public Boolean getEsGratuito() {
        return esGratuito;
    }

    public void setEsGratuito(Boolean esGratuito) {
        this.esGratuito = esGratuito;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public String getClubNombre() {
        return clubNombre;
    }

    public void setClubNombre(String clubNombre) {
        this.clubNombre = clubNombre;
    }
}
