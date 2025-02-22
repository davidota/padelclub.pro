package com.padellevel.repository;

import com.padellevel.data.Pozo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PozoRepository extends JpaRepository<Pozo, Long> {
    // Ensure this method signature exists exactly as follows:
    @Query("SELECT p FROM Pozo p LEFT JOIN FETCH p.enfrentamientos WHERE p.id = :id")
    Optional<Pozo> findByIdWithEnfrentamientos(@Param("id") Long id);
}

