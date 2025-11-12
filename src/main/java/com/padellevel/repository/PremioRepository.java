package com.padellevel.repository;

import com.padellevel.data.Premio;
import com.padellevel.data.Torneo;
import com.padellevel.data.Pozo;
import com.padellevel.data.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Premio entity operations.
 * Manages prizes awarded in tournaments and pozos.
 */
@Repository
public interface PremioRepository extends JpaRepository<Premio, Long>, JpaSpecificationExecutor<Premio> {

    /**
     * Find all prizes for a specific tournament.
     *
     * @param torneo the tournament entity
     * @return list of prizes for the specified tournament
     */
    List<Premio> findByTorneo(Torneo torneo);

    /**
     * Find prizes by tournament ID.
     *
     * @param torneoId the tournament ID
     * @return list of prizes for the specified tournament
     */
    @Query("SELECT p FROM Premio p WHERE p.torneo.id = :torneoId ORDER BY p.posicion")
    List<Premio> findByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find all prizes for a specific pozo.
     *
     * @param pozo the pozo entity
     * @return list of prizes for the specified pozo
     */
    List<Premio> findByPozo(Pozo pozo);

    /**
     * Find prizes by pozo ID.
     *
     * @param pozoId the pozo ID
     * @return list of prizes for the specified pozo
     */
    @Query("SELECT p FROM Premio p WHERE p.pozo.id = :pozoId ORDER BY p.posicion")
    List<Premio> findByPozoId(@Param("pozoId") Long pozoId);

    /**
     * Find prizes by type.
     *
     * @param tipo the prize type
     * @return list of prizes with the specified type
     */
    List<Premio> findByTipo(String tipo);

    /**
     * Find prizes awarded to a specific winning team.
     *
     * @param equipoGanador the winning team entity
     * @return list of prizes awarded to the team
     */
    List<Premio> findByEquipoGanador(Equipo equipoGanador);

    /**
     * Find prizes by team ID.
     *
     * @param equipoId the team ID
     * @return list of prizes awarded to the team
     */
    @Query("SELECT p FROM Premio p WHERE p.equipoGanador.id = :equipoId")
    List<Premio> findByEquipoGanadorId(@Param("equipoId") Long equipoId);

    /**
     * Find prizes by tournament and position.
     *
     * @param torneoId the tournament ID
     * @param posicion the position/ranking
     * @return list of prizes matching the criteria
     */
    @Query("SELECT p FROM Premio p WHERE p.torneo.id = :torneoId AND p.posicion = :posicion")
    List<Premio> findByTorneoIdAndPosicion(@Param("torneoId") Long torneoId,
                                            @Param("posicion") Integer posicion);

    /**
     * Find unclaimed prizes for a tournament.
     *
     * @param torneoId the tournament ID
     * @return list of prizes without a winning team
     */
    @Query("SELECT p FROM Premio p WHERE p.torneo.id = :torneoId AND p.equipoGanador IS NULL ORDER BY p.posicion")
    List<Premio> findUnclaimedByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find prizes with monetary value for a tournament.
     *
     * @param torneoId the tournament ID
     * @return list of prizes with monetary value
     */
    @Query("SELECT p FROM Premio p WHERE p.torneo.id = :torneoId AND p.tipo = 'MONETARIO' ORDER BY p.posicion")
    List<Premio> findMonetaryByTorneoId(@Param("torneoId") Long torneoId);
}
