package com.padellevel.services;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.Equipo;
import com.padellevel.data.User;
import com.padellevel.repository.EnfrentamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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

    @Transactional
    public List<Equipo> generarEquipos(List<User> jugadores) {
        List<Equipo> equipos = new ArrayList<>();
        List<User> barajados = new ArrayList<>(jugadores);
        Collections.shuffle(barajados);

        for (int i = 0; i < barajados.size(); i += 2) {
            if (i + 1 < barajados.size()) {
                Equipo e = new Equipo();
                e.setParticipante1(barajados.get(i));
                e.setParticipante2(barajados.get(i + 1));
                equipos.add(e);
            }
        }
        return equipos;
    }

    @Transactional
    public List<Enfrentamiento> generarEnfrentamientosConRestricciones(List<Equipo> equipos, int totalEnfrentamientosPorEquipo) {
        List<Enfrentamiento> enfrentamientos = new ArrayList<>();
        int numEquipos = equipos.size();

        // Crear una matriz para contar enfrentamientos entre equipos
        int[][] matrizEnfrentamientos = new int[numEquipos][numEquipos];

        // Generar enfrentamientos asegurando que no se enfrenten a sí mismos y respetando el número de enfrentamientos por equipo
        for (int ronda = 0; ronda < totalEnfrentamientosPorEquipo; ronda++) {
            for (int i = 0; i < numEquipos; i++) {
                for (int j = i + 1; j < numEquipos; j++) {
                    if (matrizEnfrentamientos[i][j] < totalEnfrentamientosPorEquipo &&
                        matrizEnfrentamientos[j][i] < totalEnfrentamientosPorEquipo) {
                        Enfrentamiento e = new Enfrentamiento();
                        e.setEquipo1(equipos.get(i));
                        e.setEquipo2(equipos.get(j));
                        e.setResultado(""); // Initialize as needed
                        enfrentamientos.add(e);
                        matrizEnfrentamientos[i][j]++;
                        matrizEnfrentamientos[j][i]++;
                    }
                }
            }
        }

        return enfrentamientos;
    }

    // Otros métodos según sea necesario
}