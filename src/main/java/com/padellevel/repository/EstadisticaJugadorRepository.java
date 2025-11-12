package com.padellevel.repository;

import com.padellevel.data.EstadisticaJugador;
import com.padellevel.data.User;
import com.padellevel.data.Torneo;
import com.padellevel.data.Pozo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EstadisticaJugador entity operations.
 * Manages player statistics for tournaments and pozos.
 */
@Repository
public interface EstadisticaJugadorRepository extends JpaRepository<EstadisticaJugador, Long>,
                                                       JpaSpecificationExecutor<EstadisticaJugador> {

    /**
     * Find all statistics for a specific player.
     *
     * @param jugador the player/user entity
     * @return list of statistics for the specified player
     */
    List<EstadisticaJugador> findByJugador(User jugador);

    /**
     * Find statistics by player ID.
     *
     * @param jugadorId the player ID
     * @return list of statistics for the specified player
     */
    @Query("SELECT e FROM EstadisticaJugador e WHERE e.jugador.id = :jugadorId")
    List<EstadisticaJugador> findByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find statistics for a player in a specific tournament.
     *
     * @param jugador the player/user entity
     * @param torneo the tournament entity
     * @return optional containing the statistics if found
     */
    Optional<EstadisticaJugador> findByJugadorAndTorneo(User jugador, Torneo torneo);

    /**
     * Find statistics for a player in a specific pozo.
     *
     * @param jugador the player/user entity
     * @param pozo the pozo entity
     * @return optional containing the statistics if found
     */
    Optional<EstadisticaJugador> findByJugadorAndPozo(User jugador, Pozo pozo);

    /**
     * Find all player statistics for a tournament.
     *
     * @param torneo the tournament entity
     * @return list of player statistics for the tournament
     */
    List<EstadisticaJugador> findByTorneo(Torneo torneo);

    /**
     * Find statistics by tournament ID.
     *
     * @param torneoId the tournament ID
     * @return list of player statistics for the tournament
     */
    @Query("SELECT e FROM EstadisticaJugador e WHERE e.torneo.id = :torneoId")
    List<EstadisticaJugador> findByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find all player statistics for a pozo.
     *
     * @param pozo the pozo entity
     * @return list of player statistics for the pozo
     */
    List<EstadisticaJugador> findByPozo(Pozo pozo);

    /**
     * Find statistics by pozo ID.
     *
     * @param pozoId the pozo ID
     * @return list of player statistics for the pozo
     */
    @Query("SELECT e FROM EstadisticaJugador e WHERE e.pozo.id = :pozoId")
    List<EstadisticaJugador> findByPozoId(@Param("pozoId") Long pozoId);

    /**
     * Find statistics for a player in a tournament by IDs.
     *
     * @param jugadorId the player ID
     * @param torneoId the tournament ID
     * @return optional containing the statistics if found
     */
    @Query("SELECT e FROM EstadisticaJugador e WHERE e.jugador.id = :jugadorId AND e.torneo.id = :torneoId")
    Optional<EstadisticaJugador> findByJugadorIdAndTorneoId(@Param("jugadorId") Long jugadorId,
                                                              @Param("torneoId") Long torneoId);

    /**
     * Find statistics for a player in a pozo by IDs.
     *
     * @param jugadorId the player ID
     * @param pozoId the pozo ID
     * @return optional containing the statistics if found
     */
    @Query("SELECT e FROM EstadisticaJugador e WHERE e.jugador.id = :jugadorId AND e.pozo.id = :pozoId")
    Optional<EstadisticaJugador> findByJugadorIdAndPozoId(@Param("jugadorId") Long jugadorId,
                                                            @Param("pozoId") Long pozoId);

    /**
     * Find top players by victories in a tournament.
     *
     * @param torneoId the tournament ID
     * @return list of statistics ordered by victories descending
     */
    @Query("SELECT e FROM EstadisticaJugador e WHERE e.torneo.id = :torneoId ORDER BY e.partidosGanados DESC")
    List<EstadisticaJugador> findTopPlayersByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Calculate total matches played by a player.
     *
     * @param jugadorId the player ID
     * @return total number of matches played
     */
    @Query("SELECT SUM(e.partidosJugados) FROM EstadisticaJugador e WHERE e.jugador.id = :jugadorId")
    Long calculateTotalMatchesByJugadorId(@Param("jugadorId") Long jugadorId);
}
