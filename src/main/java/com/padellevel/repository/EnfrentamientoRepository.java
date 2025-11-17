package com.padellevel.repository;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.EstadoEnfrentamiento;
import com.padellevel.data.Pozo;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnfrentamientoRepository extends JpaRepository<Enfrentamiento, Long>, JpaSpecificationExecutor<Enfrentamiento> {

    /**
     * Finds enfrentamientos by their state.
     * @param estado the match state
     * @return list of matches with the specified state
     */
    List<Enfrentamiento> findByEstado(EstadoEnfrentamiento estado);

    /**
     * Finds enfrentamientos assigned to a specific court.
     * @param pista the court number
     * @return list of matches on the specified court
     */
    List<Enfrentamiento> findByPista(Integer pista);

    /**
     * Finds enfrentamientos scheduled for a specific date and time.
     * @param fecha the scheduled date and time
     * @return list of matches scheduled for the specified time
     */
    List<Enfrentamiento> findByFechaProgramada(LocalDateTime fecha);

    /**
     * Finds enfrentamientos scheduled within a date/time range.
     * @param start the start of the range
     * @param end the end of the range
     * @return list of matches scheduled within the specified range
     */
    List<Enfrentamiento> findByFechaProgramadaBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds enfrentamientos assigned to a specific referee.
     * @param arbitro the referee user
     * @return list of matches assigned to the specified referee
     */
    List<Enfrentamiento> findByArbitro(User arbitro);

    /**
     * Finds enfrentamientos by state and scheduled within a date/time range.
     * @param estado the match state
     * @param start the start of the range
     * @param end the end of the range
     * @return list of matches matching all criteria
     */
    List<Enfrentamiento> findByEstadoAndFechaProgramadaBetween(
            EstadoEnfrentamiento estado,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Finds enfrentamientos by round number.
     * @param ronda the round number
     * @return list of matches for the specified round
     */
    List<Enfrentamiento> findByRonda(Integer ronda);

    /**
     * Finds enfrentamientos by phase number in elimination brackets.
     * @param numeroFase the phase number
     * @return list of matches for the specified phase
     */
    List<Enfrentamiento> findByNumeroFase(Integer numeroFase);

    /**
     * Finds scheduled matches (not yet finished) for a specific court.
     * @param pista the court number
     * @return list of scheduled matches on the specified court
     */
    @Query("SELECT e FROM Enfrentamiento e WHERE e.pista = :pista AND e.estado NOT IN " +
           "(com.padellevel.data.EstadoEnfrentamiento.FINALIZADO, com.padellevel.data.EstadoEnfrentamiento.CANCELADO)")
    List<Enfrentamiento> findScheduledByPista(@Param("pista") Integer pista);

    /**
     * Finds enfrentamientos by tournament ID and state.
     * @param torneoId the tournament identifier
     * @param estado the match state
     * @return list of matches matching both criteria
     */
    List<Enfrentamiento> findByTorneoIdAndEstado(Long torneoId, EstadoEnfrentamiento estado);

    /**
     * Finds enfrentamientos by pozo ID and state.
     * @param pozoId the pozo identifier
     * @param estado the match state
     * @return list of matches matching both criteria
     */
    List<Enfrentamiento> findByPozoIdAndEstado(Long pozoId, EstadoEnfrentamiento estado);

    /**
     * Finds enfrentamientos by pozo and round number.
     * Used for elimination brackets to advance winners.
     * @param pozo the pozo
     * @param ronda the round number
     * @return list of matches for the specified pozo and round
     */
    List<Enfrentamiento> findByPozoAndRonda(Pozo pozo, Integer ronda);

    /**
     * Finds enfrentamientos by pozo.
     * @param pozo the pozo
     * @return list of all matches for the specified pozo
     */
    List<Enfrentamiento> findByPozo(Pozo pozo);
}
