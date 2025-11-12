package com.padellevel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.padellevel.data.Torneo;
import com.padellevel.data.Club;
import com.padellevel.data.User;
import com.padellevel.data.EstadoTorneo;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long>, JpaSpecificationExecutor<Torneo> {

    @EntityGraph(attributePaths = "jugadores")
    List<Torneo> findAll();


    @Query("SELECT t FROM Torneo t WHERE " +
            "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.tipo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CONCAT(t.numeroEnfrentamientos, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "CONCAT(t.juegosPorEnfrentamiento, '') LIKE CONCAT('%', :searchTerm, '%')")
    List<Torneo> searchTorneos(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Torneo t JOIN FETCH t.jugadores WHERE t.id = :id")
    Torneo findByIdWithEquipos(@Param("id") Long id);

    @Query("SELECT t FROM Torneo t WHERE LOWER(t.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Torneo> searchTorneosByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.jugadores WHERE t.id = :id")
    Torneo findByIdWithJugadores(@Param("id") Long id);

    @Query("SELECT t FROM Torneo t JOIN FETCH t.jugadores WHERE t.id = :id")
    Torneo findWithJugadoresById(Long id);

    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.enfrentamientos WHERE t.id = :id")
    Torneo findByIdWithEnfrentamientos(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Torneo t LEFT JOIN FETCH t.pozos WHERE t.id = :id")
    Torneo findByIdWithPozos(@Param("id") Long id);

    /**
     * Finds all torneos by their state.
     * @param estado the tournament state
     * @return list of tournaments with the specified state
     */
    List<Torneo> findByEstado(EstadoTorneo estado);

    /**
     * Finds all torneos associated with a specific club.
     * @param club the club entity
     * @return list of tournaments for the specified club
     */
    List<Torneo> findByClub(Club club);

    /**
     * Finds all torneos by club ID.
     * @param clubId the club identifier
     * @return list of tournaments for the specified club ID
     */
    List<Torneo> findByClubId(Long clubId);

    /**
     * Finds all torneos organized by a specific user.
     * @param organizador the organizer user
     * @return list of tournaments organized by the specified user
     */
    List<Torneo> findByOrganizador(User organizador);

    /**
     * Finds torneos by whether they are free or paid.
     * @param esGratuito true for free tournaments, false for paid
     * @return list of tournaments matching the criteria
     */
    List<Torneo> findByEsGratuito(Boolean esGratuito);

    /**
     * Finds torneos by their public/private status.
     * @param esPublico true for public tournaments, false for private
     * @return list of tournaments matching the criteria
     */
    List<Torneo> findByEsPublico(Boolean esPublico);

    /**
     * Finds torneos starting within a date range.
     * @param start the start date of the range
     * @param end the end date of the range
     * @return list of tournaments starting within the specified range
     */
    List<Torneo> findByFechaInicioBetween(LocalDate start, LocalDate end);

    /**
     * Finds all active torneos (not finalized).
     * @return list of active tournaments
     */
    @Query("SELECT t FROM Torneo t WHERE t.estado <> com.padellevel.data.EstadoTorneo.FINALIZADO")
    List<Torneo> findActiveTorneos();

    /**
     * Finds torneos that are currently open for registration.
     * @return list of tournaments open for registration
     */
    @Query("SELECT t FROM Torneo t WHERE t.fechaInicioInscripcion <= CURRENT_DATE AND t.fechaCierreInscripcion >= CURRENT_DATE")
    List<Torneo> findOpenForRegistration();

    /**
     * Counts the number of torneos for a specific club.
     * @param clubId the club identifier
     * @return count of tournaments for the specified club
     */
    Long countByClubId(Long clubId);

    // ...remove any redundant queries if present...
}
