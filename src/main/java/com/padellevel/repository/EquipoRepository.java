package com.padellevel.repository;

import com.padellevel.data.Equipo;
import com.padellevel.data.Torneo;
import com.padellevel.data.Pozo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    @Query("SELECT e FROM Equipo e WHERE e.pozo.id = :pozoId")
    List<Equipo> findByPozoId(@Param("pozoId") Long pozoId);

    /**
     * Finds equipos by tournament.
     * @param torneo the tournament entity
     * @return list of teams for the specified tournament
     */
    List<Equipo> findByTorneo(Torneo torneo);

    /**
     * Finds equipos by tournament ID.
     * @param torneoId the tournament identifier
     * @return list of teams for the specified tournament
     */
    List<Equipo> findByTorneoId(Long torneoId);

    /**
     * Finds equipos by whether they are temporary American-style teams.
     * @param esTemporalAmericano true for temporary American teams, false otherwise
     * @return list of teams matching the criteria
     */
    List<Equipo> findByEsTemporalAmericano(Boolean esTemporalAmericano);

    /**
     * Finds equipos by pozo and temporal American status.
     * @param pozo the pozo entity
     * @param esTemporalAmericano true for temporary American teams, false otherwise
     * @return list of teams matching both criteria
     */
    List<Equipo> findByPozoAndEsTemporalAmericano(Pozo pozo, Boolean esTemporalAmericano);

    /**
     * Finds equipos by the American-style round number.
     * @param ronda the round number
     * @return list of teams for the specified round
     */
    List<Equipo> findByRondaAmericano(Integer ronda);
}
