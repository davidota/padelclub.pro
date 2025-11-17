package com.padellevel.repository;

import com.padellevel.data.Pista;
import com.padellevel.data.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Pista entity operations.
 * Provides methods for managing padel courts within clubs.
 */
@Repository
public interface PistaRepository extends JpaRepository<Pista, Long>, JpaSpecificationExecutor<Pista> {

    /**
     * Find all pistas belonging to a specific club.
     *
     * @param club the club entity
     * @return list of pistas in the specified club
     */
    List<Pista> findByClub(Club club);

    /**
     * Find pistas by club ID.
     *
     * @param clubId the club ID
     * @return list of pistas in the specified club
     */
    @Query("SELECT p FROM Pista p WHERE p.club.id = :clubId")
    List<Pista> findByClubId(@Param("clubId") Long clubId);

    /**
     * Find all pistas by availability status.
     *
     * @param disponible the availability status
     * @return list of pistas with the given availability status
     */
    List<Pista> findByDisponible(Boolean disponible);

    /**
     * Find a specific pista by club and court number.
     *
     * @param club the club entity
     * @param numero the court number
     * @return the pista with the given club and number
     */
    Optional<Pista> findByClubAndNumero(Club club, Integer numero);

    /**
     * Find available pistas for a specific club.
     *
     * @param club the club entity
     * @return list of available pistas in the specified club
     */
    @Query("SELECT p FROM Pista p WHERE p.club = :club AND p.disponible = true")
    List<Pista> findAvailablePistasByClub(@Param("club") Club club);

    /**
     * Find pistas by club ID and availability status.
     *
     * @param clubId the club ID
     * @param disponible the availability status
     * @return list of pistas matching the criteria
     */
    @Query("SELECT p FROM Pista p WHERE p.club.id = :clubId AND p.disponible = :disponible")
    List<Pista> findByClubIdAndDisponible(@Param("clubId") Long clubId,
                                           @Param("disponible") Boolean disponible);

    /**
     * Find a pista by its court number.
     * Note: This searches across all clubs, so numero should be unique or combined with club filter.
     *
     * @param numero the court number
     * @return optional containing the pista if found
     */
    Optional<Pista> findByNumero(Integer numero);
}
