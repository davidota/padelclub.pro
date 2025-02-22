package com.padellevel.services;

import com.padellevel.data.Torneo;
import com.padellevel.data.Equipo;
import com.padellevel.data.Pozo;
import com.padellevel.data.Enfrentamiento;
import com.padellevel.repository.TorneoRepository;
import com.padellevel.services.EnfrentamientoService;
import com.padellevel.services.PozoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final EnfrentamientoService enfrentamientoService;
    private final PozoService pozoService;

    @Autowired
    public TorneoService(TorneoRepository torneoRepository, EnfrentamientoService enfrentamientoService, PozoService pozoService) {
        this.torneoRepository = torneoRepository;
        this.enfrentamientoService = enfrentamientoService;
        this.pozoService = pozoService;
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

    // Método para recuperar un torneo por ID with pozos initialized
    @Transactional(readOnly = true)
    public Torneo findById(Long id) {
        Torneo torneo = torneoRepository.findByIdWithJugadores(id);
        if (torneo != null) {
            torneo.setJugadores(new java.util.ArrayList<>(torneo.getJugadores()));
            torneo.setPozos(new java.util.ArrayList<>(torneo.getPozos()));
        }
        return torneo;
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

    @Transactional
    public void crearTorneo(Torneo torneo) {
        torneoRepository.save(torneo);
    }

    @Transactional
    public void actualizarTorneo(Torneo torneo) {
        torneoRepository.save(torneo);
    }

    @Transactional
    public void borrarTorneo(Long id) {
        torneoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Torneo> buscarTorneos(String searchTerm) {
        return torneoRepository.searchTorneos(searchTerm);
    }

    @Transactional
    public List<Enfrentamiento> generarEnfrentamientos(Torneo torneo) {
        List<Equipo> equipos = enfrentamientoService.generarEquipos(torneo.getJugadores());
        if (equipos.size() < 2) {
            throw new IllegalArgumentException("No hay suficientes equipos para generar enfrentamientos.");
        }
        int totalEnfrentamientosPorEquipo = torneo.getJuegosPorEnfrentamiento() * torneo.getNumeroDeVueltas();
        return enfrentamientoService.generarEnfrentamientosConRestricciones(equipos, totalEnfrentamientosPorEquipo);
    }

    @Transactional
    public Torneo addPozoToTorneo(Long torneoId, Pozo pozo) {
        Torneo torneo = findById(torneoId); // Fetches torneo with pozos initialized
        if (pozo.getId() == null) {
            // For a new pozo, add it directly
            torneo.getPozos().add(pozo);
        } else {
            Optional<Pozo> opt = pozoService.findById(pozo.getId());
            Pozo managedPozo = opt.orElse(pozo);
            boolean exists = torneo.getPozos().stream()
                .anyMatch(p -> p.getId() != null && p.getId().equals(managedPozo.getId()));
            if (!exists) {
                torneo.getPozos().add(managedPozo);
            }
        }
        return torneoRepository.save(torneo);
    }
}