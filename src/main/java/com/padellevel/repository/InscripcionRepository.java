package com.padellevel.repository;

import com.padellevel.data.EstadoInscripcion;
import com.padellevel.data.Inscripcion;
import com.padellevel.data.User;
import com.padellevel.data.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Inscripcion entity operations.
 * Manages tournament registrations for players.
 */
@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long>,
                                                JpaSpecificationExecutor<Inscripcion> {

    /**
     * Find all inscriptions for a specific player.
     *
     * @param jugador the player/user entity
     * @return list of inscriptions for the specified player
     */
    List<Inscripcion> findByJugador(User jugador);

    /**
     * Find all inscriptions for a specific tournament.
     *
     * @param torneo the tournament entity
     * @return list of inscriptions for the specified tournament
     */
    List<Inscripcion> findByTorneo(Torneo torneo);

    /**
     * Find inscriptions for a tournament with a specific status.
     *
     * @param torneo the tournament entity
     * @param estado the inscription status
     * @return list of inscriptions matching the criteria
     */
    List<Inscripcion> findByTorneoAndEstado(Torneo torneo, String estado);

    /**
     * Find a specific inscription for a player in a tournament.
     *
     * @param jugador the player/user entity
     * @param torneo the tournament entity
     * @return optional containing the inscription if found
     */
    Optional<Inscripcion> findByJugadorAndTorneo(User jugador, Torneo torneo);

    /**
     * Count total inscriptions for a tournament.
     *
     * @param torneo the tournament entity
     * @return the number of inscriptions for the tournament
     */
    Long countByTorneo(Torneo torneo);

    /**
     * Count confirmed inscriptions for a tournament.
     *
     * @param torneoId the tournament ID
     * @param estado the inscription status
     * @return the number of confirmed inscriptions
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.torneo.id = :torneoId AND i.estado = :estado")
    Long countByTorneoIdAndEstado(@Param("torneoId") Long torneoId, @Param("estado") String estado);

    /**
     * Find inscriptions by player ID.
     *
     * @param jugadorId the player ID
     * @return list of inscriptions for the specified player
     */
    @Query("SELECT i FROM Inscripcion i WHERE i.jugador.id = :jugadorId ORDER BY i.fechaInscripcion DESC")
    List<Inscripcion> findByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find confirmed inscriptions for a tournament.
     *
     * @param torneoId the tournament ID
     * @return list of confirmed inscriptions
     */
    @Query("SELECT i FROM Inscripcion i WHERE i.torneo.id = :torneoId AND i.estado = 'CONFIRMADA'")
    List<Inscripcion> findConfirmedByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Check if a player is already inscribed in a tournament.
     *
     * @param jugadorId the player ID
     * @param torneoId the tournament ID
     * @return true if the player is already inscribed
     */
    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.jugador.id = :jugadorId AND i.torneo.id = :torneoId")
    boolean existsByJugadorIdAndTorneoId(@Param("jugadorId") Long jugadorId, @Param("torneoId") Long torneoId);

    /**
     * Find inscriptions for a tournament with a specific status (using enum).
     *
     * @param torneo the tournament entity
     * @param estado the inscription status enum
     * @return list of inscriptions matching the criteria
     */
    List<Inscripcion> findByTorneoAndEstado(Torneo torneo, EstadoInscripcion estado);

    /**
     * Count inscriptions for a tournament with a specific status (using enum).
     *
     * @param torneo the tournament entity
     * @param estado the inscription status enum
     * @return the number of inscriptions matching the criteria
     */
    Long countByTorneoAndEstado(Torneo torneo, EstadoInscripcion estado);
}
