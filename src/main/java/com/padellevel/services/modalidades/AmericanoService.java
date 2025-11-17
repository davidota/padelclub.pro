package com.padellevel.services.modalidades;

import com.padellevel.data.*;
import com.padellevel.repository.EnfrentamientoRepository;
import com.padellevel.repository.EquipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la modalidad Americano.
 *
 * En esta modalidad:
 * - Los jugadores rotan de pareja en cada ronda
 * - Se genera un ranking individual (no por equipo)
 * - Cada jugador juega con diferentes compañeros
 * - Los equipos son temporales y cambian en cada ronda
 */
@Service
public class AmericanoService implements ModalidadService {

    private static final Logger logger = LoggerFactory.getLogger(AmericanoService.class);

    private final EnfrentamientoRepository enfrentamientoRepository;
    private final EquipoRepository equipoRepository;

    public AmericanoService(EnfrentamientoRepository enfrentamientoRepository,
                           EquipoRepository equipoRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
        this.equipoRepository = equipoRepository;
    }

    @Override
    @Transactional
    public List<Enfrentamiento> generarEnfrentamientos(Pozo pozo, List<Equipo> equiposBase) {
        logger.info("Generando enfrentamientos para modalidad Americano. Pozo: {}", pozo.getNombre());

        // En Americano trabajamos con jugadores individuales, no equipos fijos
        List<User> jugadores = extraerJugadores(equiposBase);
        int numeroJugadores = jugadores.size();

        if (!validarNumeroEquipos(numeroJugadores / 2)) {
            throw new IllegalArgumentException("Número de jugadores inválido para Americano: " + numeroJugadores);
        }

        int numeroRondas = pozo.getNumeroDeRondas() != null ? pozo.getNumeroDeRondas() :
                          calcularNumeroRondasOptimo(numeroJugadores);

        List<Enfrentamiento> todosEnfrentamientos = new ArrayList<>();

        // Generar enfrentamientos para cada ronda con rotación de parejas
        for (int ronda = 1; ronda <= numeroRondas; ronda++) {
            logger.debug("Generando ronda {} de {}", ronda, numeroRondas);

            // Crear parejas para esta ronda
            List<Equipo> equiposRonda = crearParejasParaRonda(pozo, jugadores, ronda);

            // Generar enfrentamientos entre las parejas de esta ronda
            List<Enfrentamiento> enfrentamientosRonda = generarEnfrentamientosRonda(pozo, equiposRonda, ronda);

            todosEnfrentamientos.addAll(enfrentamientosRonda);
        }

        logger.info("Generados {} enfrentamientos en {} rondas para Americano",
                   todosEnfrentamientos.size(), numeroRondas);

        return todosEnfrentamientos;
    }

    /**
     * Extrae la lista de jugadores individuales de los equipos base.
     */
    private List<User> extraerJugadores(List<Equipo> equipos) {
        List<User> jugadores = new ArrayList<>();
        for (Equipo equipo : equipos) {
            if (equipo.getParticipante1() != null) {
                jugadores.add(equipo.getParticipante1());
            }
            if (equipo.getParticipante2() != null) {
                jugadores.add(equipo.getParticipante2());
            }
        }
        return jugadores;
    }

    /**
     * Calcula el número óptimo de rondas basado en el número de jugadores.
     * Idealmente, cada jugador debería jugar con todos los demás al menos una vez.
     */
    private int calcularNumeroRondasOptimo(int numeroJugadores) {
        // Fórmula: n-1 rondas para que cada jugador juegue con todos (n jugadores)
        return Math.max(numeroJugadores - 1, 3); // Mínimo 3 rondas
    }

    /**
     * Crea parejas temporales para una ronda específica.
     * Usa un algoritmo de rotación para asegurar variedad de parejas.
     */
    private List<Equipo> crearParejasParaRonda(Pozo pozo, List<User> jugadores, int ronda) {
        List<Equipo> equipos = new ArrayList<>();
        List<User> jugadoresRotados = rotarJugadores(jugadores, ronda);

        // Emparejar jugadores: 1 con último, 2 con penúltimo, etc.
        for (int i = 0; i < jugadoresRotados.size() / 2; i++) {
            User jugador1 = jugadoresRotados.get(i);
            User jugador2 = jugadoresRotados.get(jugadoresRotados.size() - 1 - i);

            Equipo equipo = new Equipo();
            equipo.setParticipante1(jugador1);
            equipo.setParticipante2(jugador2);
            equipo.setPozo(pozo);
            equipo.setTorneo(pozo.getTorneo());
            equipo.setNombreEquipo(jugador1.getName() + " & " + jugador2.getName());
            equipo.setEsTemporalAmericano(true);
            equipo.setRondaAmericano(ronda);

            equipos.add(equipoRepository.save(equipo));
        }

        return equipos;
    }

    /**
     * Algoritmo de rotación de jugadores para variar las parejas.
     */
    private List<User> rotarJugadores(List<User> jugadores, int ronda) {
        List<User> rotados = new ArrayList<>(jugadores);

        // Rotación circular: mover elementos según el número de ronda
        int rotacion = (ronda - 1) % jugadores.size();
        for (int i = 0; i < rotacion; i++) {
            User primero = rotados.remove(0);
            rotados.add(primero);
        }

        return rotados;
    }

    /**
     * Genera los enfrentamientos entre las parejas de una ronda.
     */
    private List<Enfrentamiento> generarEnfrentamientosRonda(Pozo pozo, List<Equipo> equipos, int ronda) {
        List<Enfrentamiento> enfrentamientos = new ArrayList<>();

        // Generar partidos: cada pareja juega contra otra pareja
        for (int i = 0; i < equipos.size(); i += 2) {
            if (i + 1 < equipos.size()) {
                Enfrentamiento enfrentamiento = new Enfrentamiento();
                enfrentamiento.setTorneo(pozo.getTorneo());
                enfrentamiento.setPozo(pozo);
                enfrentamiento.setEquipo1(equipos.get(i));
                enfrentamiento.setEquipo2(equipos.get(i + 1));
                enfrentamiento.setRonda(ronda);
                enfrentamiento.setEstado(EstadoEnfrentamiento.PENDIENTE_PROGRAMACION);

                enfrentamientos.add(enfrentamientoRepository.save(enfrentamiento));
            }
        }

        return enfrentamientos;
    }

    @Override
    public boolean validarNumeroEquipos(int numeroEquipos) {
        // En Americano necesitamos un número par de jugadores (mínimo 4 = 2 equipos)
        return numeroEquipos >= 2 && numeroEquipos % 2 == 0;
    }

    @Override
    public int getMinEquipos() {
        return 2; // Mínimo 4 jugadores = 2 equipos
    }

    @Override
    public int getMaxEquiposRecomendado() {
        return 8; // 16 jugadores para mantener manejable
    }

    @Override
    public int calcularNumeroPartidos(int numeroEquipos) {
        int numeroJugadores = numeroEquipos * 2;
        int numeroRondas = calcularNumeroRondasOptimo(numeroJugadores);
        // En cada ronda: numeroJugadores / 4 partidos (2 jugadores por equipo, 4 jugadores por partido)
        return (numeroJugadores / 4) * numeroRondas;
    }

    @Override
    public String getDescripcion() {
        return "Modalidad Americano: Los jugadores rotan de pareja en cada ronda. " +
               "El ranking es individual, no por equipos. Cada jugador tiene la oportunidad " +
               "de jugar con diferentes compañeros a lo largo del torneo.";
    }
}
