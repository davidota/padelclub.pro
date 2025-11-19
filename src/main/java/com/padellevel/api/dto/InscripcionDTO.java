package com.padellevel.api.dto;

import com.padellevel.data.EstadoInscripcion;
import com.padellevel.data.Inscripcion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para exponer información de inscripciones en la API REST.
 */
@Schema(description = "Información de una inscripción a torneo")
public class InscripcionDTO {

    @Schema(description = "ID de la inscripción", example = "1")
    private Long id;

    @Schema(description = "ID del jugador", example = "5")
    private Long jugadorId;

    @Schema(description = "Nombre del jugador", example = "Juan Pérez")
    private String jugadorNombre;

    @Schema(description = "ID del torneo", example = "3")
    private Long torneoId;

    @Schema(description = "Nombre del torneo", example = "Torneo de Verano 2025")
    private String torneoNombre;

    @Schema(description = "ID de la pareja (opcional)", example = "6")
    private Long parejaId;

    @Schema(description = "Nombre de la pareja", example = "María García")
    private String parejaNombre;

    @Schema(description = "Estado de la inscripción", example = "CONFIRMADA")
    private EstadoInscripcion estado;

    @Schema(description = "Fecha de inscripción")
    private LocalDateTime fechaInscripcion;

    @Schema(description = "Comentarios del jugador")
    private String comentarios;

    @Schema(description = "ID del pago asociado")
    private Long pagoId;

    @Schema(description = "Estado del pago", example = "COMPLETADO")
    private String pagoEstado;

    // Constructor vacío
    public InscripcionDTO() {}

    // Constructor desde entidad
    public InscripcionDTO(Inscripcion inscripcion) {
        this.id = inscripcion.getId();
        if (inscripcion.getJugador() != null) {
            this.jugadorId = inscripcion.getJugador().getId();
            this.jugadorNombre = inscripcion.getJugador().getNombreCompleto();
        }
        if (inscripcion.getTorneo() != null) {
            this.torneoId = inscripcion.getTorneo().getId();
            this.torneoNombre = inscripcion.getTorneo().getNombre();
        }
        if (inscripcion.getPareja() != null) {
            this.parejaId = inscripcion.getPareja().getId();
            this.parejaNombre = inscripcion.getPareja().getNombreCompleto();
        }
        this.estado = inscripcion.getEstado();
        this.fechaInscripcion = inscripcion.getFechaInscripcion();
        this.comentarios = inscripcion.getComentarios();
        if (inscripcion.getPago() != null) {
            this.pagoId = inscripcion.getPago().getId();
            this.pagoEstado = inscripcion.getPago().getEstado().toString();
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(Long jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getJugadorNombre() {
        return jugadorNombre;
    }

    public void setJugadorNombre(String jugadorNombre) {
        this.jugadorNombre = jugadorNombre;
    }

    public Long getTorneoId() {
        return torneoId;
    }

    public void setTorneoId(Long torneoId) {
        this.torneoId = torneoId;
    }

    public String getTorneoNombre() {
        return torneoNombre;
    }

    public void setTorneoNombre(String torneoNombre) {
        this.torneoNombre = torneoNombre;
    }

    public Long getParejaId() {
        return parejaId;
    }

    public void setParejaId(Long parejaId) {
        this.parejaId = parejaId;
    }

    public String getParejaNombre() {
        return parejaNombre;
    }

    public void setParejaNombre(String parejaNombre) {
        this.parejaNombre = parejaNombre;
    }

    public EstadoInscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoInscripcion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }

    public String getPagoEstado() {
        return pagoEstado;
    }

    public void setPagoEstado(String pagoEstado) {
        this.pagoEstado = pagoEstado;
    }
}
