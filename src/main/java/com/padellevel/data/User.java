package com.padellevel.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad Usuario que representa a un jugador, administrador u operador del sistema.
 */
@Entity
@Table(name = "application_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
public class User extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @Email
    @Column(unique = true)
    private String email;

    /**
     * Google ID para autenticación OAuth2.
     */
    @Column(name = "google_id")
    private String googleId;

    private String name;

    private String apellido;

    @JsonIgnore
    private String hashedPassword;

    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    /**
     * Biografía del jugador para perfil social.
     */
    @Column(length = 500)
    private String bio;

    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    /**
     * Nivel de habilidad del jugador.
     */
    @Enumerated(EnumType.STRING)
    private NivelJugador nivel;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    /**
     * Inscripciones del jugador en torneos.
     */
    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Inscripcion> inscripciones = new ArrayList<>();

    /**
     * Estadísticas del jugador.
     */
    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EstadisticaJugador> estadisticas = new ArrayList<>();

    /**
     * Notificaciones recibidas.
     */
    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Notificacion> notificaciones = new ArrayList<>();

    /**
     * Logros desbloqueados por el jugador.
     */
    @ManyToMany(mappedBy = "jugadores")
    @JsonIgnore
    private List<Logro> logros = new ArrayList<>();

    /**
     * Experiencia total acumulada en el sistema de gamificación.
     */
    @Column(name = "experiencia_total")
    private Integer experienciaTotal = 0;

    /**
     * Nivel de gamificación del jugador (1-100).
     */
    @Column(name = "nivel_gamificacion")
    private Integer nivelGamificacion = 1;

    /**
     * Indica si el usuario está activo en el sistema.
     */
    private Boolean activo = true;

    /**
     * Fecha y hora del último acceso del usuario.
     */
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    /**
     * Clubes de los cuales el usuario es miembro.
     */
    @ManyToMany(mappedBy = "miembros")
    @JsonIgnore
    private List<Club> clubes = new ArrayList<>();

    /**
     * Clubes en los que el usuario es staff.
     */
    @ManyToMany(mappedBy = "staff")
    @JsonIgnore
    private List<Club> clubesStaff = new ArrayList<>();

    /**
     * Club actual seleccionado por el usuario (para contexto de sesión).
     * No persiste, se gestiona en sesión.
     */
    @Transient
    private Club clubActual;

    /**
     * Campo transitorio para contraseña en texto plano (no se persiste).
     */
    @Transient
    private String password;

    // Getters y setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getPassword() {
        return password; // Return the transient password
    }

    public void setPassword(String password) {
        this.password = password; // Set the transient password
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<Role> getRole() {

        return roles;

    }

    public void setRole(Set<Role> role) {
        this.roles = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public NivelJugador getNivel() {
        return nivel;
    }

    public void setNivel(NivelJugador nivel) {
        this.nivel = nivel;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public List<EstadisticaJugador> getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(List<EstadisticaJugador> estadisticas) {
        this.estadisticas = estadisticas;
    }

    public List<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public List<Logro> getLogros() {
        return logros;
    }

    public void setLogros(List<Logro> logros) {
        this.logros = logros;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Integer getExperienciaTotal() {
        return experienciaTotal != null ? experienciaTotal : 0;
    }

    public void setExperienciaTotal(Integer experienciaTotal) {
        this.experienciaTotal = experienciaTotal;
    }

    public Integer getNivelGamificacion() {
        return nivelGamificacion != null ? nivelGamificacion : 1;
    }

    public void setNivelGamificacion(Integer nivelGamificacion) {
        this.nivelGamificacion = nivelGamificacion;
    }

    /**
     * Método de conveniencia para obtener el nombre completo.
     */
    public String getNombreCompleto() {
        return name + (apellido != null ? " " + apellido : "");
    }

    /**
     * Calcula la XP necesaria para alcanzar un nivel específico.
     * Fórmula: nivel * 100 + (nivel - 1) * 50
     */
    public static int calcularXPParaNivel(int nivel) {
        if (nivel <= 1) return 0;
        return nivel * 100 + (nivel - 1) * 50;
    }

    /**
     * Calcula la XP necesaria para el siguiente nivel.
     */
    public int getXPParaSiguienteNivel() {
        return calcularXPParaNivel(getNivelGamificacion() + 1);
    }

    /**
     * Calcula la XP necesaria para el nivel actual.
     */
    public int getXPParaNivelActual() {
        return calcularXPParaNivel(getNivelGamificacion());
    }

    /**
     * Calcula el progreso hacia el siguiente nivel (0-100%).
     */
    public double getProgresoNivel() {
        int xpActual = getExperienciaTotal();
        int xpNivelActual = getXPParaNivelActual();
        int xpSiguienteNivel = getXPParaSiguienteNivel();

        if (xpSiguienteNivel <= xpNivelActual) return 100.0;

        double progreso = ((double) (xpActual - xpNivelActual) / (xpSiguienteNivel - xpNivelActual)) * 100;
        return Math.max(0, Math.min(100, progreso));
    }

    /**
     * Verifica si el usuario tiene un rol específico.
     */
    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Verifica si el usuario es administrador.
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Verifica si el usuario es jugador.
     */
    public boolean isPlayer() {
        return hasRole(Role.PLAYER);
    }

    public List<Club> getClubes() {
        return clubes;
    }

    public void setClubes(List<Club> clubes) {
        this.clubes = clubes;
    }

    public List<Club> getClubesStaff() {
        return clubesStaff;
    }

    public void setClubesStaff(List<Club> clubesStaff) {
        this.clubesStaff = clubesStaff;
    }

    public Club getClubActual() {
        return clubActual;
    }

    public void setClubActual(Club clubActual) {
        this.clubActual = clubActual;
    }

    /**
     * Verifica si el usuario pertenece a algún club.
     */
    public boolean tieneClubs() {
        return clubes != null && !clubes.isEmpty();
    }

    /**
     * Verifica si el usuario es miembro de un club específico.
     */
    public boolean esMiembroDeClub(Club club) {
        return clubes != null && clubes.contains(club);
    }

    /**
     * Verifica si el usuario es staff de un club específico.
     */
    public boolean esStaffDeClub(Club club) {
        return clubesStaff != null && clubesStaff.contains(club);
    }

    /**
     * Verifica si el usuario tiene permisos de gestión en un club.
     */
    public boolean tienePermisosGestionEnClub(Club club) {
        if (club == null) return false;
        return club.esAdministrador(this) || club.esStaff(this);
    }

    /**
     * Obtiene todos los clubes donde el usuario tiene permisos (miembro, staff o admin).
     */
    public List<Club> getTodosLosClubes() {
        List<Club> todos = new ArrayList<>();
        if (clubes != null) {
            todos.addAll(clubes);
        }
        if (clubesStaff != null) {
            for (Club club : clubesStaff) {
                if (!todos.contains(club)) {
                    todos.add(club);
                }
            }
        }
        return todos;
    }
}
