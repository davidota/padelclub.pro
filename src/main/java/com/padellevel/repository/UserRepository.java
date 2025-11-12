package com.padellevel.repository;

import com.padellevel.data.NivelJugador;
import com.padellevel.data.Role;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad User.
 * Proporciona acceso a datos de usuarios, jugadores y administradores.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Busca un usuario por su nombre de usuario.
     */
    User findByUsername(String username);

    /**
     * Busca un usuario por su email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su Google ID (OAuth2).
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Verifica si existe un usuario con el username dado.
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el Google ID dado.
     */
    boolean existsByGoogleId(String googleId);

    /**
     * Busca usuarios activos.
     */
    List<User> findByActivo(Boolean activo);

    /**
     * Busca usuarios por nivel de habilidad.
     */
    List<User> findByNivel(NivelJugador nivel);

    /**
     * Busca usuarios por rol específico.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.activo = true")
    List<User> findByRole(@Param("role") Role role);

    /**
     * Busca jugadores activos (rol PLAYER).
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'PLAYER' AND u.activo = true")
    List<User> findActivePlayers();

    /**
     * Busca administradores activos (rol ADMIN).
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'ADMIN' AND u.activo = true")
    List<User> findActiveAdmins();

    /**
     * Busca usuarios por término de búsqueda (username, name, apellido, email).
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Actualiza la fecha de último acceso del usuario.
     */
    @Modifying
    @Query("UPDATE User u SET u.ultimoAcceso = :ultimoAcceso WHERE u.id = :userId")
    void updateUltimoAcceso(@Param("userId") Long userId, @Param("ultimoAcceso") LocalDateTime ultimoAcceso);

    /**
     * Busca usuarios inactivos desde hace más de X días.
     */
    @Query("SELECT u FROM User u WHERE u.activo = true AND u.ultimoAcceso < :fecha")
    List<User> findInactiveUsersSince(@Param("fecha") LocalDateTime fecha);

    /**
     * Cuenta usuarios activos por nivel.
     */
    @Query("SELECT u.nivel, COUNT(u) FROM User u WHERE u.activo = true GROUP BY u.nivel")
    List<Object[]> countUsersByNivel();
}
