package com.padellevel.repository;

import com.padellevel.data.Club;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Club entity operations.
 * Provides methods for CRUD operations and custom queries for clubs.
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {

    /**
     * Find a club by its name.
     *
     * @param nombre the name of the club
     * @return the club with the given name, or null if not found
     */
    Club findByNombre(String nombre);

    /**
     * Find all clubs by their active status.
     *
     * @param activo the active status
     * @return list of clubs with the given active status
     */
    List<Club> findByActivo(Boolean activo);

    /**
     * Search clubs by name or city.
     *
     * @param searchTerm the search term to match against club name or city
     * @return list of clubs matching the search criteria
     */
    @Query("SELECT c FROM Club c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.ciudad) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Club> searchClubes(@Param("searchTerm") String searchTerm);

    /**
     * Search clubs by name.
     *
     * @param nombre the name to search for
     * @return list of clubs with names containing the search term
     */
    @Query("SELECT c FROM Club c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Club> searchByNombre(@Param("nombre") String nombre);

    /**
     * Find active clubs in a specific city.
     *
     * @param ciudad the city name
     * @return list of active clubs in the specified city
     */
    @Query("SELECT c FROM Club c WHERE c.activo = true AND LOWER(c.ciudad) = LOWER(:ciudad)")
    List<Club> findActiveClubesByCity(@Param("ciudad") String ciudad);

    /**
     * Encuentra todos los clubes donde el usuario es miembro.
     *
     * @param user el usuario
     * @return lista de clubes donde el usuario es miembro
     */
    @Query("SELECT c FROM Club c JOIN c.miembros m WHERE m = :user AND c.activo = true")
    List<Club> findClubesByMiembro(@Param("user") User user);

    /**
     * Encuentra todos los clubes donde el usuario es staff.
     *
     * @param user el usuario
     * @return lista de clubes donde el usuario es staff
     */
    @Query("SELECT c FROM Club c JOIN c.staff s WHERE s = :user AND c.activo = true")
    List<Club> findClubesByStaff(@Param("user") User user);

    /**
     * Encuentra todos los clubes administrados por un usuario.
     *
     * @param administrador el usuario administrador
     * @return lista de clubes administrados por el usuario
     */
    List<Club> findByAdministrador(User administrador);

    /**
     * Encuentra todos los clubes donde el usuario tiene algún rol (miembro, staff o admin).
     *
     * @param user el usuario
     * @return lista de clubes donde el usuario participa
     */
    @Query("SELECT DISTINCT c FROM Club c LEFT JOIN c.miembros m LEFT JOIN c.staff s " +
           "WHERE (m = :user OR s = :user OR c.administrador = :user) AND c.activo = true")
    List<Club> findAllClubesByUsuario(@Param("user") User user);

    /**
     * Cuenta el número de miembros de un club.
     *
     * @param club el club
     * @return número de miembros
     */
    @Query("SELECT COUNT(m) FROM Club c JOIN c.miembros m WHERE c = :club")
    long countMiembrosByClub(@Param("club") Club club);

    /**
     * Cuenta el número de torneos activos de un club.
     *
     * @param club el club
     * @return número de torneos activos
     */
    @Query("SELECT COUNT(t) FROM Torneo t WHERE t.club = :club AND t.estado != 'FINALIZADO' AND t.estado != 'CANCELADO'")
    long countTorneosActivosByClub(@Param("club") Club club);
}
