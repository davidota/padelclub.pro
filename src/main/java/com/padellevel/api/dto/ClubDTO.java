package com.padellevel.api.dto;

import com.padellevel.data.Club;
import com.padellevel.data.ConfiguracionClub;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de Club a través de la API REST.
 */
public class ClubDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String ciudad;
    private String pais;
    private String telefono;
    private String email;
    private String website;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Información del administrador
    private Long administradorId;
    private String administradorNombre;

    // Estadísticas
    private Integer totalMiembros;
    private Integer totalStaff;
    private Integer totalPistas;
    private Integer totalTorneos;

    // Configuración
    private String moneda;
    private String zonaHoraria;
    private String idioma;
    private String colorPrimario;
    private String colorSecundario;

    // Constructor vacío
    public ClubDTO() {}

    // Constructor desde entidad
    public ClubDTO(Club club) {
        this.id = club.getId();
        this.nombre = club.getNombre();
        this.descripcion = club.getDescripcion();
        this.direccion = club.getDireccion();
        this.ciudad = club.getCiudad();
        this.pais = club.getPais();
        this.telefono = club.getTelefono();
        this.email = club.getEmail();
        this.website = club.getWebsite();
        this.activo = club.getActivo();
        this.fechaCreacion = club.getFechaCreacion();

        if (club.getAdministrador() != null) {
            this.administradorId = club.getAdministrador().getId();
            this.administradorNombre = club.getAdministrador().getNombreCompleto();
        }

        // Estadísticas básicas
        this.totalMiembros = club.getMiembros() != null ? club.getMiembros().size() : 0;
        this.totalStaff = club.getStaff() != null ? club.getStaff().size() : 0;
        this.totalPistas = club.getPistas() != null ? club.getPistas().size() : 0;
        this.totalTorneos = club.getTorneos() != null ? club.getTorneos().size() : 0;

        // Configuración
        if (club.getConfiguracion() != null) {
            ConfiguracionClub config = club.getConfiguracion();
            this.moneda = config.getMoneda();
            this.zonaHoraria = config.getZonaHoraria();
            this.idioma = config.getIdioma();
            this.colorPrimario = config.getColorPrimario();
            this.colorSecundario = config.getColorSecundario();
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getAdministradorId() {
        return administradorId;
    }

    public void setAdministradorId(Long administradorId) {
        this.administradorId = administradorId;
    }

    public String getAdministradorNombre() {
        return administradorNombre;
    }

    public void setAdministradorNombre(String administradorNombre) {
        this.administradorNombre = administradorNombre;
    }

    public Integer getTotalMiembros() {
        return totalMiembros;
    }

    public void setTotalMiembros(Integer totalMiembros) {
        this.totalMiembros = totalMiembros;
    }

    public Integer getTotalStaff() {
        return totalStaff;
    }

    public void setTotalStaff(Integer totalStaff) {
        this.totalStaff = totalStaff;
    }

    public Integer getTotalPistas() {
        return totalPistas;
    }

    public void setTotalPistas(Integer totalPistas) {
        this.totalPistas = totalPistas;
    }

    public Integer getTotalTorneos() {
        return totalTorneos;
    }

    public void setTotalTorneos(Integer totalTorneos) {
        this.totalTorneos = totalTorneos;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getColorPrimario() {
        return colorPrimario;
    }

    public void setColorPrimario(String colorPrimario) {
        this.colorPrimario = colorPrimario;
    }

    public String getColorSecundario() {
        return colorSecundario;
    }

    public void setColorSecundario(String colorSecundario) {
        this.colorSecundario = colorSecundario;
    }
}
