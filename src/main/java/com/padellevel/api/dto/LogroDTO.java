package com.padellevel.api.dto;

import com.padellevel.data.Logro;
import com.padellevel.data.LogroUsuario;
import com.padellevel.data.TipoLogro;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para exponer información de logros en la API REST.
 */
@Schema(description = "Información de un logro")
public class LogroDTO {

    @Schema(description = "ID del logro", example = "1")
    private Long id;

    @Schema(description = "Nombre del logro", example = "Primera Victoria")
    private String nombre;

    @Schema(description = "Descripción del logro", example = "Gana tu primer partido")
    private String descripcion;

    @Schema(description = "Icono del logro", example = "🏆")
    private String icono;

    @Schema(description = "Tipo de logro", example = "VICTORIAS")
    private TipoLogro tipo;

    @Schema(description = "Meta numérica", example = "1")
    private Integer meta;

    @Schema(description = "Indica si el usuario ha desbloqueado este logro", example = "true")
    private Boolean desbloqueado;

    @Schema(description = "Progreso actual hacia el logro", example = "3")
    private Integer progresoActual;

    @Schema(description = "Porcentaje de progreso", example = "75.0")
    private Double porcentajeProgreso;

    @Schema(description = "Fecha de desbloqueo")
    private LocalDateTime fechaDesbloqueo;

    // Constructor vacío
    public LogroDTO() {}

    // Constructor desde entidad Logro
    public LogroDTO(Logro logro) {
        this.id = logro.getId();
        this.nombre = logro.getNombre();
        this.descripcion = logro.getDescripcion();
        this.icono = logro.getIcono();
        this.tipo = logro.getTipo();
        this.meta = logro.getMeta();
        this.desbloqueado = false;
        this.progresoActual = 0;
        this.porcentajeProgreso = 0.0;
    }

    // Constructor desde LogroUsuario (logro desbloqueado)
    public LogroDTO(LogroUsuario logroUsuario) {
        this(logroUsuario.getLogro());
        this.desbloqueado = logroUsuario.isCompleto();
        this.progresoActual = logroUsuario.getProgresoActual();
        this.porcentajeProgreso = logroUsuario.getPorcentajeProgreso();
        this.fechaDesbloqueo = logroUsuario.getFechaDesbloqueo();
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

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public TipoLogro getTipo() {
        return tipo;
    }

    public void setTipo(TipoLogro tipo) {
        this.tipo = tipo;
    }

    public Integer getMeta() {
        return meta;
    }

    public void setMeta(Integer meta) {
        this.meta = meta;
    }

    public Boolean getDesbloqueado() {
        return desbloqueado;
    }

    public void setDesbloqueado(Boolean desbloqueado) {
        this.desbloqueado = desbloqueado;
    }

    public Integer getProgresoActual() {
        return progresoActual;
    }

    public void setProgresoActual(Integer progresoActual) {
        this.progresoActual = progresoActual;
    }

    public Double getPorcentajeProgreso() {
        return porcentajeProgreso;
    }

    public void setPorcentajeProgreso(Double porcentajeProgreso) {
        this.porcentajeProgreso = porcentajeProgreso;
    }

    public LocalDateTime getFechaDesbloqueo() {
        return fechaDesbloqueo;
    }

    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) {
        this.fechaDesbloqueo = fechaDesbloqueo;
    }
}
