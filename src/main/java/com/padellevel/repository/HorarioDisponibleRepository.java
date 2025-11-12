package com.padellevel.repository;

import com.padellevel.data.HorarioDisponible;
import com.padellevel.data.Pista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for HorarioDisponible entity operations.
 * Manages available time slots for padel courts.
 */
@Repository
public interface HorarioDisponibleRepository extends JpaRepository<HorarioDisponible, Long>,
                                                     JpaSpecificationExecutor<HorarioDisponible> {

    /**
     * Find all available time slots for a specific pista.
     *
     * @param pista the pista entity
     * @return list of time slots for the specified pista
     */
    List<HorarioDisponible> findByPista(Pista pista);

    /**
     * Find time slots for a specific pista on a given date.
     *
     * @param pista the pista entity
     * @param fecha the date
     * @return list of time slots for the specified pista and date
     */
    List<HorarioDisponible> findByPistaAndFecha(Pista pista, LocalDate fecha);

    /**
     * Find time slots by availability status.
     *
     * @param disponible the availability status
     * @return list of time slots with the given availability status
     */
    List<HorarioDisponible> findByDisponible(Boolean disponible);

    /**
     * Find available time slots for a specific pista.
     *
     * @param pista the pista entity
     * @param disponible the availability status
     * @return list of time slots matching the criteria
     */
    List<HorarioDisponible> findByPistaAndDisponible(Pista pista, Boolean disponible);

    /**
     * Find available time slots for a pista on a specific date.
     *
     * @param pista the pista entity
     * @param fecha the date
     * @param disponible the availability status
     * @return list of time slots matching the criteria
     */
    @Query("SELECT h FROM HorarioDisponible h WHERE h.pista = :pista AND h.fecha = :fecha AND h.disponible = :disponible")
    List<HorarioDisponible> findByPistaAndFechaAndDisponible(@Param("pista") Pista pista,
                                                               @Param("fecha") LocalDate fecha,
                                                               @Param("disponible") Boolean disponible);

    /**
     * Find a specific time slot by pista, date, and time.
     *
     * @param pista the pista entity
     * @param fecha the date
     * @param horaInicio the start time
     * @return optional containing the time slot if found
     */
    @Query("SELECT h FROM HorarioDisponible h WHERE h.pista = :pista AND h.fecha = :fecha AND h.horaInicio = :horaInicio")
    Optional<HorarioDisponible> findByPistaAndFechaAndHoraInicio(@Param("pista") Pista pista,
                                                                   @Param("fecha") LocalDate fecha,
                                                                   @Param("horaInicio") LocalTime horaInicio);

    /**
     * Find available time slots for a club on a specific date.
     *
     * @param clubId the club ID
     * @param fecha the date
     * @return list of available time slots for the club on the specified date
     */
    @Query("SELECT h FROM HorarioDisponible h WHERE h.pista.club.id = :clubId AND h.fecha = :fecha AND h.disponible = true")
    List<HorarioDisponible> findAvailableByClubAndDate(@Param("clubId") Long clubId,
                                                         @Param("fecha") LocalDate fecha);

    /**
     * Find time slots by pista ID.
     *
     * @param pistaId the pista ID
     * @return list of time slots for the specified pista
     */
    @Query("SELECT h FROM HorarioDisponible h WHERE h.pista.id = :pistaId ORDER BY h.fecha, h.horaInicio")
    List<HorarioDisponible> findByPistaId(@Param("pistaId") Long pistaId);
}
