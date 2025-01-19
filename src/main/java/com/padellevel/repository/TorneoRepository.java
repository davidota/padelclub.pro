package com.padellevel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.padellevel.data.Torneo;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long>, JpaSpecificationExecutor<Torneo> {

    @EntityGraph(attributePaths = "jugadores")
    List<Torneo> findAll();


    @Query("SELECT t FROM Torneo t WHERE " +
            "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.tipo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CONCAT(t.numeroEnfrentamientos, '') LIKE CONCAT('%', :searchTerm, '%') OR " +
            "CONCAT(t.juegosPorEnfrentamiento, '') LIKE CONCAT('%', :searchTerm, '%')")
    List<Torneo> searchTorneos(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Torneo t JOIN FETCH t.jugadores WHERE t.id = :id")
    Torneo findByIdWithEquipos(@Param("id") Long id);

    @Query("SELECT t FROM Torneo t WHERE LOWER(t.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Torneo> searchTorneosByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Torneo t LEFT JOIN FETCH t.jugadores WHERE t.id = :id")
    Torneo findByIdWithJugadores(@Param("id") Long id);

    @Query("SELECT t FROM Torneo t JOIN FETCH t.jugadores WHERE t.id = :id")
    Torneo findWithJugadoresById(Long id);
    
    // ...remove any redundant queries if present...
}
