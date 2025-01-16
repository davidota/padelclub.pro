package com.padellevel.services;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.repository.EnfrentamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnfrentamientoService {

    private final EnfrentamientoRepository enfrentamientoRepository;

    @Autowired
    public EnfrentamientoService(EnfrentamientoRepository enfrentamientoRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
    }

    // Método para guardar un solo enfrentamiento
    public void save(Enfrentamiento enfrentamiento) {
        enfrentamientoRepository.save(enfrentamiento);
    }

    // Método para guardar múltiples enfrentamientos
    public void saveAll(List<Enfrentamiento> enfrentamientos) {
        enfrentamientoRepository.saveAll(enfrentamientos);
    }

    // Método para recuperar todos los enfrentamientos
    public List<Enfrentamiento> findAll() {
        return enfrentamientoRepository.findAll();
    }

    // Otros métodos según sea necesario
}