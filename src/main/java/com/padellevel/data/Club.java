package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un club de pádel.
 * Soporta múltiples clubes en el sistema, cada uno con sus propias pistas y torneos.
 */
@Entity
@Table(name = "club")
public class Club extends AbstractEntity {

    /**
     * Nombre del club.
     */
    @NotBlank
    @Column(nullable = false)
    private String nombre;

    /**
     * Dirección física del club.
     */
    @NotBlank
    @Column(nullable = false)
    private String direccion;

    /**
     * Ciudad donde se encuentra el club.
     */
    @NotBlank
    @Column(nullable = false)
    private String ciudad;

    /**
     * País donde se encuentra el club.
     */
    @NotBlank
    @Column(nullable = false)
    private String pais;

    /**
     * Número de teléfono del club.
     */
    private String telefono;

    /**
     * Correo electrónico del club.
     */
    @Email
    private String email;

    /**
     * Sitio web del club.
     */
    private String website;

    /**
     * Logo del club en formato binario.
     */
    @Lob
    @Column(length = 1000000)
    private byte[] logo;

    /**
     * Administrador del club.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private User administrador;

    /**
     * Lista de torneos organizados por este club.
     */
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Torneo> torneos = new ArrayList<>();

    /**
     * Lista de pistas del club.
     */
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pista> pistas = new ArrayList<>();

    /**
     * Indica si el club está activo.
     */
    @Column(nullable = false)
    private Boolean activo = true;

    // Constructores
    public Club() {}

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public User getAdministrador() {
        return administrador;
    }

    public void setAdministrador(User administrador) {
        this.administrador = administrador;
    }

    public List<Torneo> getTorneos() {
        return torneos;
    }

    public void setTorneos(List<Torneo> torneos) {
        this.torneos = torneos;
    }

    public List<Pista> getPistas() {
        return pistas;
    }

    public void setPistas(List<Pista> pistas) {
        this.pistas = pistas;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
