package com.padellevel.repository;

import com.padellevel.data.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    @Query("SELECT e FROM Equipo e WHERE e.pozo.id = :pozoId")
    List<Equipo> findByPozoId(@Param("pozoId") Long pozoId);
}
