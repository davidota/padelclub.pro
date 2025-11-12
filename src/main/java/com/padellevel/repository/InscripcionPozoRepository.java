package com.padellevel.repository;

import com.padellevel.data.InscripcionPozo;
import com.padellevel.data.User;
import com.padellevel.data.Pozo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InscripcionPozo entity operations.
 * Manages pool (pozo) registrations for players.
 */
@Repository
public interface InscripcionPozoRepository extends JpaRepository<InscripcionPozo, Long>,
                                                    JpaSpecificationExecutor<InscripcionPozo> {

    /**
     * Find all pozo inscriptions for a specific player.
     *
     * @param jugador the player/user entity
     * @return list of pozo inscriptions for the specified player
     */
    List<InscripcionPozo> findByJugador(User jugador);

    /**
     * Find all inscriptions for a specific pozo.
     *
     * @param pozo the pozo entity
     * @return list of inscriptions for the specified pozo
     */
    List<InscripcionPozo> findByPozo(Pozo pozo);

    /**
     * Find inscriptions for a pozo with a specific status.
     *
     * @param pozo the pozo entity
     * @param estado the inscription status
     * @return list of inscriptions matching the criteria
     */
    List<InscripcionPozo> findByPozoAndEstado(Pozo pozo, String estado);

    /**
     * Count total inscriptions for a pozo.
     *
     * @param pozo the pozo entity
     * @return the number of inscriptions for the pozo
     */
    Long countByPozo(Pozo pozo);

    /**
     * Find a specific inscription for a player in a pozo.
     *
     * @param jugador the player/user entity
     * @param pozo the pozo entity
     * @return optional containing the inscription if found
     */
    Optional<InscripcionPozo> findByJugadorAndPozo(User jugador, Pozo pozo);

    /**
     * Count confirmed inscriptions for a pozo.
     *
     * @param pozoId the pozo ID
     * @param estado the inscription status
     * @return the number of confirmed inscriptions
     */
    @Query("SELECT COUNT(ip) FROM InscripcionPozo ip WHERE ip.pozo.id = :pozoId AND ip.estado = :estado")
    Long countByPozoIdAndEstado(@Param("pozoId") Long pozoId, @Param("estado") String estado);

    /**
     * Find inscriptions by player ID.
     *
     * @param jugadorId the player ID
     * @return list of pozo inscriptions for the specified player
     */
    @Query("SELECT ip FROM InscripcionPozo ip WHERE ip.jugador.id = :jugadorId ORDER BY ip.fechaInscripcion DESC")
    List<InscripcionPozo> findByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find confirmed inscriptions for a pozo.
     *
     * @param pozoId the pozo ID
     * @return list of confirmed inscriptions
     */
    @Query("SELECT ip FROM InscripcionPozo ip WHERE ip.pozo.id = :pozoId AND ip.estado = 'CONFIRMADA'")
    List<InscripcionPozo> findConfirmedByPozoId(@Param("pozoId") Long pozoId);

    /**
     * Check if a player is already inscribed in a pozo.
     *
     * @param jugadorId the player ID
     * @param pozoId the pozo ID
     * @return true if the player is already inscribed
     */
    @Query("SELECT COUNT(ip) > 0 FROM InscripcionPozo ip WHERE ip.jugador.id = :jugadorId AND ip.pozo.id = :pozoId")
    boolean existsByJugadorIdAndPozoId(@Param("jugadorId") Long jugadorId, @Param("pozoId") Long pozoId);
}
