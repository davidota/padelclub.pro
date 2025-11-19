package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
     * Descripción del club.
     */
    @Column(length = 2000)
    private String descripcion;

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
     * Administrador principal del club.
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private User administrador;

    /**
     * Staff/empleados del club con permisos de gestión.
     */
    @ManyToMany
    @JoinTable(
        name = "club_staff",
        joinColumns = @JoinColumn(name = "club_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> staff = new ArrayList<>();

    /**
     * Miembros del club.
     */
    @ManyToMany
    @JoinTable(
        name = "club_miembros",
        joinColumns = @JoinColumn(name = "club_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> miembros = new ArrayList<>();

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
     * Configuración específica del club.
     */
    @Embedded
    private ConfiguracionClub configuracion = new ConfiguracionClub();

    /**
     * Fecha de creación del club en el sistema.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el club está activo.
     */
    @Column(nullable = false)
    private Boolean activo = true;

    // Constructores
    public Club() {}

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<User> getStaff() {
        return staff;
    }

    public void setStaff(List<User> staff) {
        this.staff = staff;
    }

    public List<User> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<User> miembros) {
        this.miembros = miembros;
    }

    public ConfiguracionClub getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(ConfiguracionClub configuracion) {
        this.configuracion = configuracion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Agrega un miembro al club.
     */
    public void agregarMiembro(User user) {
        if (!miembros.contains(user)) {
            miembros.add(user);
        }
    }

    /**
     * Elimina un miembro del club.
     */
    public void eliminarMiembro(User user) {
        miembros.remove(user);
    }

    /**
     * Agrega un staff al club.
     */
    public void agregarStaff(User user) {
        if (!staff.contains(user)) {
            staff.add(user);
        }
    }

    /**
     * Elimina un staff del club.
     */
    public void eliminarStaff(User user) {
        staff.remove(user);
    }

    /**
     * Verifica si un usuario es miembro del club.
     */
    public boolean esMiembro(User user) {
        return miembros.contains(user);
    }

    /**
     * Verifica si un usuario es staff del club.
     */
    public boolean esStaff(User user) {
        return staff.contains(user);
    }

    /**
     * Verifica si un usuario es administrador del club.
     */
    public boolean esAdministrador(User user) {
        return administrador != null && administrador.equals(user);
    }

    /**
     * Verifica si un usuario tiene permisos de gestión (admin o staff).
     */
    public boolean tienePermisosGestion(User user) {
        return esAdministrador(user) || esStaff(user);
    }

    /**
     * Obtiene el total de miembros del club.
     */
    public int getTotalMiembros() {
        return miembros != null ? miembros.size() : 0;
    }
}
