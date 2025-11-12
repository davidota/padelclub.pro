package com.padellevel.repository;

import com.padellevel.data.Pozo;
import com.padellevel.data.Torneo;
import com.padellevel.data.ModalidadPozo;
import com.padellevel.data.EstadoPozo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PozoRepository extends JpaRepository<Pozo, Long> {
    // Ensure this method signature exists exactly as follows:
    @Query("SELECT p FROM Pozo p LEFT JOIN FETCH p.enfrentamientos WHERE p.id = :id")
    Optional<Pozo> findByIdWithEnfrentamientos(@Param("id") Long id);

    /**
     * Finds pozos by their modality (e.g., Masculino, Femenino, Mixto).
     * @param modalidad the pozo modality
     * @return list of pozos with the specified modality
     */
    List<Pozo> findByModalidad(ModalidadPozo modalidad);

    /**
     * Finds pozos by their state.
     * @param estado the pozo state
     * @return list of pozos with the specified state
     */
    List<Pozo> findByEstado(EstadoPozo estado);

    /**
     * Finds pozos by tournament and modality.
     * @param torneo the tournament entity
     * @param modalidad the pozo modality
     * @return list of pozos matching both criteria
     */
    List<Pozo> findByTorneoAndModalidad(Torneo torneo, ModalidadPozo modalidad);

    /**
     * Finds pozos by whether they are free or paid.
     * @param esGratuito true for free pozos, false for paid
     * @return list of pozos matching the criteria
     */
    List<Pozo> findByEsGratuito(Boolean esGratuito);

    /**
     * Finds pozos that are currently open for registration.
     * @return list of pozos with state INSCRIPCION_ABIERTA
     */
    @Query("SELECT p FROM Pozo p WHERE p.estado = com.padellevel.data.EstadoPozo.INSCRIPCION_ABIERTA")
    List<Pozo> findOpenForRegistration();

    /**
     * Finds pozos by tournament ID and state.
     * @param torneoId the tournament identifier
     * @param estado the pozo state
     * @return list of pozos matching both criteria
     */
    List<Pozo> findByTorneoIdAndEstado(Long torneoId, EstadoPozo estado);

    /**
     * Counts the number of pozos for a specific tournament.
     * @param torneoId the tournament identifier
     * @return count of pozos for the specified tournament
     */
    Long countByTorneoId(Long torneoId);
}

