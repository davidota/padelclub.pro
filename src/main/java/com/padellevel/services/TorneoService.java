package com.padellevel.services;

import com.padellevel.data.Torneo;
import com.padellevel.repository.TorneoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;

    @Autowired
    public TorneoService(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

    // Método para guardar un torneo
    public void save(Torneo torneo) {
        torneoRepository.save(torneo);
    }

    // Método para recuperar todos los torneos
    @Transactional(readOnly = true)
    public List<Torneo> findAll() {
        return torneoRepository.findAll();
    }

    // Método para recuperar un torneo por ID con jugadores inicializados
    @Transactional(readOnly = true)
    public Torneo findById(Long id) {
        return torneoRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        torneoRepository.deleteById(id);
    }

    public void delete(Torneo torneo) {
        torneoRepository.delete(torneo);
    }

    public List<Torneo> searchTorneos(String searchTerm) {
        return torneoRepository.searchTorneos(searchTerm);
    }

    @Transactional(readOnly = true)
    public Torneo findByIdWithJugadores(Long id) {
        return torneoRepository.findByIdWithJugadores(id);
        
    }
}
