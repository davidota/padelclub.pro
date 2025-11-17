package com.padellevel.services.modalidades;

import com.padellevel.data.Enfrentamiento;
import com.padellevel.data.Equipo;
import com.padellevel.data.EstadoEnfrentamiento;
import com.padellevel.data.Pozo;
import com.padellevel.repository.EnfrentamientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la modalidad Todos contra Todos (Round Robin).
 *
 * En esta modalidad:
 * - Cada equipo juega contra todos los demás equipos
 * - Sistema de puntos: Victoria = 3 puntos, Empate = 1 punto, Derrota = 0 puntos
 * - El equipo con más puntos al final gana
 * - Se pueden configurar múltiples vueltas (ida, vuelta, etc.)
 */
@Service
public class RoundRobinService implements ModalidadService {

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinService.class);

    private final EnfrentamientoRepository enfrentamientoRepository;

    public RoundRobinService(EnfrentamientoRepository enfrentamientoRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
    }

    @Override
    @Transactional
    public List<Enfrentamiento> generarEnfrentamientos(Pozo pozo, List<Equipo> equipos) {
        logger.info("Generando enfrentamientos para modalidad Todos contra Todos. Pozo: {}, Equipos: {}",
                   pozo.getNombre(), equipos.size());

        if (!validarNumeroEquipos(equipos.size())) {
            throw new IllegalArgumentException("Número de equipos inválido para Round Robin: " + equipos.size());
        }

        // Determinar número de vueltas (por defecto 1)
        int numeroVueltas = pozo.getTorneo().getNumeroDeVueltas() != null ?
                           pozo.getTorneo().getNumeroDeVueltas() : 1;

        List<Enfrentamiento> todosEnfrentamientos = new ArrayList<>();

        // Generar enfrentamientos para cada vuelta
        for (int vuelta = 1; vuelta <= numeroVueltas; vuelta++) {
            logger.debug("Generando vuelta {} de {}", vuelta, numeroVueltas);

            List<Enfrentamiento> enfrentamientosVuelta;

            if (equipos.size() % 2 == 0) {
                // Número par de equipos: usar algoritmo de rotación circular
                enfrentamientosVuelta = generarEnfrentamientosCircular(pozo, equipos, vuelta);
            } else {
                // Número impar: un equipo descansa cada ronda
                enfrentamientosVuelta = generarEnfrentamientosConDescanso(pozo, equipos, vuelta);
            }

            todosEnfrentamientos.addAll(enfrentamientosVuelta);
        }

        logger.info("Generados {} enfrentamientos en {} vuelta(s) para Round Robin",
                   todosEnfrentamientos.size(), numeroVueltas);

        return todosEnfrentamientos;
    }

    /**
     * Algoritmo de rotación circular para número par de equipos.
     * Asegura que cada equipo juegue contra todos los demás exactamente una vez por vuelta.
     */
    private List<Enfrentamiento> generarEnfrentamientosCircular(Pozo pozo, List<Equipo> equipos, int vuelta) {
        List<Enfrentamiento> enfrentamientos = new ArrayList<>();
        int n = equipos.size();
        int numeroRondas = n - 1; // n-1 rondas para que todos jueguen contra todos

        // Crear matriz de equipos para rotación
        List<Equipo> equiposRotacion = new ArrayList<>(equipos);

        for (int ronda = 0; ronda < numeroRondas; ronda++) {
            int rondaActual = (vuelta - 1) * numeroRondas + ronda + 1;

            // Generar partidos para esta ronda
            for (int i = 0; i < n / 2; i++) {
                Equipo equipo1 = equiposRotacion.get(i);
                Equipo equipo2 = equiposRotacion.get(n - 1 - i);

                Enfrentamiento enfrentamiento = crearEnfrentamiento(pozo, equipo1, equipo2, rondaActual);
                enfrentamientos.add(enfrentamiento);
            }

            // Rotar equipos (excepto el primero que permanece fijo)
            if (ronda < numeroRondas - 1) {
                Equipo ultimo = equiposRotacion.remove(n - 1);
                equiposRotacion.add(1, ultimo);
            }
        }

        return enfrentamientos;
    }

    /**
     * Genera enfrentamientos cuando hay un número impar de equipos.
     * Un equipo descansa en cada ronda.
     */
    private List<Enfrentamiento> generarEnfrentamientosConDescanso(Pozo pozo, List<Equipo> equipos, int vuelta) {
        List<Enfrentamiento> enfrentamientos = new ArrayList<>();
        int n = equipos.size();
        int numeroRondas = n; // n rondas para que cada equipo descanse una vez

        // Agregar equipo "fantasma" para hacer par
        List<Equipo> equiposConNull = new ArrayList<>(equipos);
        equiposConNull.add(null);

        for (int ronda = 0; ronda < numeroRondas; ronda++) {
            int rondaActual = (vuelta - 1) * numeroRondas + ronda + 1;

            // Generar partidos para esta ronda (el que está con null descansa)
            for (int i = 0; i < (n + 1) / 2; i++) {
                Equipo equipo1 = equiposConNull.get(i);
                Equipo equipo2 = equiposConNull.get(n - i);

                // Si alguno es null (el fantasma), ese equipo descansa
                if (equipo1 != null && equipo2 != null) {
                    Enfrentamiento enfrentamiento = crearEnfrentamiento(pozo, equipo1, equipo2, rondaActual);
                    enfrentamientos.add(enfrentamiento);
                } else {
                    logger.debug("Equipo {} descansa en ronda {}",
                               equipo1 != null ? equipo1.getNombreEquipo() : equipo2.getNombreEquipo(),
                               rondaActual);
                }
            }

            // Rotar (mantener primero fijo)
            if (ronda < numeroRondas - 1) {
                Equipo ultimo = equiposConNull.remove(n);
                equiposConNull.add(1, ultimo);
            }
        }

        return enfrentamientos;
    }

    /**
     * Crea un enfrentamiento entre dos equipos.
     */
    private Enfrentamiento crearEnfrentamiento(Pozo pozo, Equipo equipo1, Equipo equipo2, int ronda) {
        Enfrentamiento enfrentamiento = new Enfrentamiento();
        enfrentamiento.setTorneo(pozo.getTorneo());
        enfrentamiento.setPozo(pozo);
        enfrentamiento.setEquipo1(equipo1);
        enfrentamiento.setEquipo2(equipo2);
        enfrentamiento.setRonda(ronda);
        enfrentamiento.setEstado(EstadoEnfrentamiento.PENDIENTE_PROGRAMACION);

        return enfrentamientoRepository.save(enfrentamiento);
    }

    @Override
    public boolean validarNumeroEquipos(int numeroEquipos) {
        // Round Robin requiere mínimo 3 equipos
        return numeroEquipos >= 3;
    }

    @Override
    public int getMinEquipos() {
        return 3;
    }

    @Override
    public int getMaxEquiposRecomendado() {
        return 12; // Más de 12 equipos genera muchos partidos
    }

    @Override
    public int calcularNumeroPartidos(int numeroEquipos) {
        // Fórmula: C(n,2) = n*(n-1)/2 para una vuelta
        // Para múltiples vueltas: multiplicar por el número de vueltas
        return (numeroEquipos * (numeroEquipos - 1)) / 2;
    }

    @Override
    public String getDescripcion() {
        return "Modalidad Todos contra Todos (Round Robin): Cada equipo juega contra todos los demás equipos. " +
               "Sistema de puntos: Victoria = 3 puntos, Empate = 1 punto, Derrota = 0 puntos. " +
               "El equipo con más puntos al final es el ganador. " +
               "Se pueden configurar múltiples vueltas (ida, vuelta, etc.).";
    }

    /**
     * Calcula los puntos de un equipo basándose en sus resultados.
     * Victoria = 3 puntos, Empate = 1 punto, Derrota = 0 puntos.
     *
     * @param victorias Número de victorias
     * @param empates Número de empates
     * @param derrotas Número de derrotas
     * @return Total de puntos
     */
    public int calcularPuntos(int victorias, int empates, int derrotas) {
        return (victorias * 3) + (empates * 1);
    }
}
