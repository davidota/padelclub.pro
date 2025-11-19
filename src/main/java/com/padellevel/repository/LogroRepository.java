package com.padellevel.repository;

import com.padellevel.data.Club;
import com.padellevel.data.Logro;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Logro entity operations.
 * Manages achievements earned by players.
 */
@Repository
public interface LogroRepository extends JpaRepository<Logro, Long>, JpaSpecificationExecutor<Logro> {

    /**
     * Find achievements by type.
     *
     * @param tipo the achievement type
     * @return list of achievements with the specified type
     */
    List<Logro> findByTipo(String tipo);

    /**
     * Find achievements containing a specific player.
     *
     * @param jugadores the player to search for
     * @return list of achievements containing the player
     */
    @Query("SELECT l FROM Logro l JOIN l.jugadores j WHERE j = :jugador")
    List<Logro> findByJugadores(@Param("jugador") User jugadores);

    /**
     * Find achievements by player ID.
     *
     * @param jugadorId the player ID
     * @return list of achievements for the specified player
     */
    @Query("SELECT l FROM Logro l JOIN l.jugadores j WHERE j.id = :jugadorId ORDER BY l.fechaObtencion DESC")
    List<Logro> findByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find achievements by player ID and type.
     *
     * @param jugadorId the player ID
     * @param tipo the achievement type
     * @return list of achievements matching the criteria
     */
    @Query("SELECT l FROM Logro l JOIN l.jugadores j WHERE j.id = :jugadorId AND l.tipo = :tipo")
    List<Logro> findByJugadorIdAndTipo(@Param("jugadorId") Long jugadorId,
                                        @Param("tipo") String tipo);

    /**
     * Count achievements for a player.
     *
     * @param jugadorId the player ID
     * @return the number of achievements for the player
     */
    @Query("SELECT COUNT(l) FROM Logro l JOIN l.jugadores j WHERE j.id = :jugadorId")
    Long countByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find recent achievements for a player.
     *
     * @param jugadorId the player ID
     * @param limit the maximum number of results
     * @return list of recent achievements
     */
    @Query("SELECT l FROM Logro l JOIN l.jugadores j WHERE j.id = :jugadorId ORDER BY l.fechaObtencion DESC")
    List<Logro> findRecentByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find achievements by tournament ID.
     *
     * @param torneoId the tournament ID
     * @return list of achievements related to the tournament
     */
    @Query("SELECT l FROM Logro l WHERE l.torneo.id = :torneoId")
    List<Logro> findByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find achievements by pozo ID.
     *
     * @param pozoId the pozo ID
     * @return list of achievements related to the pozo
     */
    @Query("SELECT l FROM Logro l WHERE l.pozo.id = :pozoId")
    List<Logro> findByPozoId(@Param("pozoId") Long pozoId);

    /**
     * Find all unique achievement types.
     *
     * @return list of distinct achievement types
     */
    @Query("SELECT DISTINCT l.tipo FROM Logro l ORDER BY l.tipo")
    List<String> findDistinctTipos();

    /**
     * Encuentra logros por club.
     *
     * @param club el club
     * @return lista de logros del club
     */
    List<Logro> findByClub(Club club);

    /**
     * Encuentra logros globales (sin club específico).
     *
     * @return lista de logros globales
     */
    List<Logro> findByClubIsNull();

    /**
     * Encuentra logros por club o globales.
     *
     * @param club el club (puede ser null para obtener solo globales)
     * @return lista de logros del club y globales
     */
    @Query("SELECT l FROM Logro l WHERE l.club = :club OR l.club IS NULL")
    List<Logro> findByClubOrGlobal(@Param("club") Club club);

    /**
     * Encuentra logros de un jugador en un club específico.
     *
     * @param jugador el jugador
     * @param club el club
     * @return lista de logros del jugador en el club
     */
    @Query("SELECT l FROM Logro l JOIN l.jugadores j WHERE j = :jugador AND (l.club = :club OR l.club IS NULL)")
    List<Logro> findByJugadorAndClub(@Param("jugador") User jugador, @Param("club") Club club);

    /**
     * Cuenta logros por club.
     *
     * @param club el club
     * @return número de logros del club
     */
    @Query("SELECT COUNT(l) FROM Logro l WHERE l.club = :club")
    long countByClub(@Param("club") Club club);
}
