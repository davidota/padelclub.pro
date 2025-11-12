package com.padellevel.repository;

import com.padellevel.data.EstadisticaEquipo;
import com.padellevel.data.Equipo;
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
 * Repository interface for EstadisticaEquipo entity operations.
 * Manages team statistics for tournaments and pozos.
 */
@Repository
public interface EstadisticaEquipoRepository extends JpaRepository<EstadisticaEquipo, Long>,
                                                      JpaSpecificationExecutor<EstadisticaEquipo> {

    /**
     * Find all statistics for a specific team.
     *
     * @param equipo the team entity
     * @return list of statistics for the specified team
     */
    List<EstadisticaEquipo> findByEquipo(Equipo equipo);

    /**
     * Find statistics by team ID.
     *
     * @param equipoId the team ID
     * @return list of statistics for the specified team
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.equipo.id = :equipoId")
    List<EstadisticaEquipo> findByEquipoId(@Param("equipoId") Long equipoId);

    /**
     * Find statistics for a team in a specific tournament.
     *
     * @param equipo the team entity
     * @param torneo the tournament entity
     * @return optional containing the statistics if found
     */
    Optional<EstadisticaEquipo> findByEquipoAndTorneo(Equipo equipo, Torneo torneo);

    /**
     * Find statistics for a team in a specific pozo.
     *
     * @param equipo the team entity
     * @param pozo the pozo entity
     * @return optional containing the statistics if found
     */
    Optional<EstadisticaEquipo> findByEquipoAndPozo(Equipo equipo, Pozo pozo);

    /**
     * Find all team statistics for a tournament.
     *
     * @param torneo the tournament entity
     * @return list of team statistics for the tournament
     */
    List<EstadisticaEquipo> findByTorneo(Torneo torneo);

    /**
     * Find statistics by tournament ID.
     *
     * @param torneoId the tournament ID
     * @return list of team statistics for the tournament
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.torneo.id = :torneoId")
    List<EstadisticaEquipo> findByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find all team statistics for a pozo.
     *
     * @param pozo the pozo entity
     * @return list of team statistics for the pozo
     */
    List<EstadisticaEquipo> findByPozo(Pozo pozo);

    /**
     * Find statistics by pozo ID.
     *
     * @param pozoId the pozo ID
     * @return list of team statistics for the pozo
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.pozo.id = :pozoId")
    List<EstadisticaEquipo> findByPozoId(@Param("pozoId") Long pozoId);

    /**
     * Find statistics for a team in a tournament by IDs.
     *
     * @param equipoId the team ID
     * @param torneoId the tournament ID
     * @return optional containing the statistics if found
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.equipo.id = :equipoId AND e.torneo.id = :torneoId")
    Optional<EstadisticaEquipo> findByEquipoIdAndTorneoId(@Param("equipoId") Long equipoId,
                                                            @Param("torneoId") Long torneoId);

    /**
     * Find statistics for a team in a pozo by IDs.
     *
     * @param equipoId the team ID
     * @param pozoId the pozo ID
     * @return optional containing the statistics if found
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.equipo.id = :equipoId AND e.pozo.id = :pozoId")
    Optional<EstadisticaEquipo> findByEquipoIdAndPozoId(@Param("equipoId") Long equipoId,
                                                          @Param("pozoId") Long pozoId);

    /**
     * Find top teams by victories in a tournament.
     *
     * @param torneoId the tournament ID
     * @return list of statistics ordered by victories descending
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.torneo.id = :torneoId ORDER BY e.partidosGanados DESC")
    List<EstadisticaEquipo> findTopTeamsByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find team statistics by position in a tournament.
     *
     * @param torneoId the tournament ID
     * @param posicion the position/ranking
     * @return list of teams at the specified position
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.torneo.id = :torneoId AND e.posicion = :posicion")
    List<EstadisticaEquipo> findByTorneoIdAndPosicion(@Param("torneoId") Long torneoId,
                                                        @Param("posicion") Integer posicion);

    /**
     * Find tournament rankings ordered by position.
     *
     * @param torneoId the tournament ID
     * @return list of team statistics ordered by position
     */
    @Query("SELECT e FROM EstadisticaEquipo e WHERE e.torneo.id = :torneoId ORDER BY e.posicion ASC")
    List<EstadisticaEquipo> findRankingByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Calculate average points per team in a tournament.
     *
     * @param torneoId the tournament ID
     * @return average points
     */
    @Query("SELECT AVG(e.puntos) FROM EstadisticaEquipo e WHERE e.torneo.id = :torneoId")
    Double calculateAveragePointsByTorneoId(@Param("torneoId") Long torneoId);
}
