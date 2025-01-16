package com.padellevel.repository;

import com.padellevel.data.Enfrentamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnfrentamientoRepository extends JpaRepository<Enfrentamiento, Long> {
    // Métodos adicionales si son necesarios
}
