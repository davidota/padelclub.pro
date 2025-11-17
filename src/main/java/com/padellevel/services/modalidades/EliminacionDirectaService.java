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
import java.util.Collections;
import java.util.List;

/**
 * Implementación de la modalidad Eliminación Directa.
 *
 * En esta modalidad:
 * - Los equipos compiten en un bracket de eliminación
 * - El perdedor queda eliminado del torneo
 * - El ganador avanza a la siguiente fase
 * - Fases: 32avos, 16avos, Octavos, Cuartos, Semifinales, Final
 * - Se pueden agregar "byes" si el número no es potencia de 2
 */
@Service
public class EliminacionDirectaService implements ModalidadService {

    private static final Logger logger = LoggerFactory.getLogger(EliminacionDirectaService.class);

    private final EnfrentamientoRepository enfrentamientoRepository;

    public EliminacionDirectaService(EnfrentamientoRepository enfrentamientoRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
    }

    @Override
    @Transactional
    public List<Enfrentamiento> generarEnfrentamientos(Pozo pozo, List<Equipo> equipos) {
        logger.info("Generando enfrentamientos para modalidad Eliminación Directa. Pozo: {}, Equipos: {}",
                   pozo.getNombre(), equipos.size());

        if (!validarNumeroEquipos(equipos.size())) {
            throw new IllegalArgumentException("Número de equipos inválido para Eliminación Directa: " + equipos.size());
        }

        // Mezclar equipos para sembrar aleatoriamente
        List<Equipo> equiposMezclados = new ArrayList<>(equipos);
        Collections.shuffle(equiposMezclados);

        // Calcular la potencia de 2 más cercana (bracket size)
        int bracketSize = calcularBracketSize(equipos.size());
        int numeroFaseInicial = bracketSize;

        logger.info("Bracket size: {}, Fase inicial: {}avos de final", bracketSize, numeroFaseInicial);

        List<Enfrentamiento> enfrentamientos = new ArrayList<>();

        // Generar primera ronda con byes si es necesario
        List<Enfrentamiento> primeraRonda = generarPrimeraRonda(pozo, equiposMezclados, numeroFaseInicial);
        enfrentamientos.addAll(primeraRonda);

        // Generar enfrentamientos placeholder para fases posteriores
        generarFasesPosteriores(pozo, numeroFaseInicial, enfrentamientos);

        logger.info("Generados {} enfrentamientos para Eliminación Directa", enfrentamientos.size());

        return enfrentamientos;
    }

    /**
     * Calcula el tamaño del bracket (potencia de 2 más cercana).
     */
    private int calcularBracketSize(int numeroEquipos) {
        int potencia = 1;
        while (potencia < numeroEquipos) {
            potencia *= 2;
        }
        return potencia;
    }

    /**
     * Genera la primera ronda del bracket, incluyendo byes si es necesario.
     */
    private List<Enfrentamiento> generarPrimeraRonda(Pozo pozo, List<Equipo> equipos, int numeroFase) {
        List<Enfrentamiento> enfrentamientos = new ArrayList<>();
        int bracketSize = numeroFase;
        int numeroPartidos = bracketSize / 2;

        for (int i = 0; i < numeroPartidos; i++) {
            Equipo equipo1 = i < equipos.size() ? equipos.get(i) : null;
            Equipo equipo2 = (numeroPartidos + i) < equipos.size() ? equipos.get(numeroPartidos + i) : null;

            // Si algún equipo es null, es un bye (el otro avanza automáticamente)
            if (equipo1 != null || equipo2 != null) {
                Enfrentamiento enfrentamiento = new Enfrentamiento();
                enfrentamiento.setTorneo(pozo.getTorneo());
                enfrentamiento.setPozo(pozo);
                enfrentamiento.setEquipo1(equipo1);
                enfrentamiento.setEquipo2(equipo2);
                enfrentamiento.setRonda(1);
                enfrentamiento.setNumeroFase(numeroFase);

                // Si es un bye, marcar como walkover
                if (equipo1 == null || equipo2 == null) {
                    enfrentamiento.setEstado(EstadoEnfrentamiento.WALKOVER);
                    logger.debug("Generado BYE: {} avanza automáticamente",
                               equipo1 != null ? equipo1.getNombreEquipo() : equipo2.getNombreEquipo());
                } else {
                    enfrentamiento.setEstado(EstadoEnfrentamiento.PENDIENTE_PROGRAMACION);
                }

                enfrentamientos.add(enfrentamientoRepository.save(enfrentamiento));
            }
        }

        return enfrentamientos;
    }

    /**
     * Genera enfrentamientos placeholder para fases posteriores del bracket.
     * Estos se llenarán automáticamente cuando avancen los ganadores.
     */
    private void generarFasesPosteriores(Pozo pozo, int faseInicial, List<Enfrentamiento> enfrentamientos) {
        int faseActual = faseInicial / 2;
        int ronda = 2;

        while (faseActual >= 1) {
            int numeroPartidos = faseActual / 2;

            for (int i = 0; i < numeroPartidos; i++) {
                Enfrentamiento enfrentamiento = new Enfrentamiento();
                enfrentamiento.setTorneo(pozo.getTorneo());
                enfrentamiento.setPozo(pozo);
                // Los equipos se asignarán cuando se conozcan los ganadores
                enfrentamiento.setEquipo1(null);
                enfrentamiento.setEquipo2(null);
                enfrentamiento.setRonda(ronda);
                enfrentamiento.setNumeroFase(faseActual);
                enfrentamiento.setEstado(EstadoEnfrentamiento.PENDIENTE_PROGRAMACION);

                enfrentamientos.add(enfrentamientoRepository.save(enfrentamiento));
            }

            faseActual /= 2;
            ronda++;
        }
    }

    /**
     * Avanza un equipo ganador a la siguiente fase del bracket.
     *
     * @param enfrentamientoActual Enfrentamiento que se acaba de completar
     * @param equipoGanador Equipo ganador que avanza
     */
    @Transactional
    public void avanzarGanador(Enfrentamiento enfrentamientoActual, Equipo equipoGanador) {
        if (enfrentamientoActual.getNumeroFase() == 1) {
            logger.info("¡Torneo finalizado! Ganador: {}", equipoGanador.getNombreEquipo());
            return; // Es la final, no hay más fases
        }

        int siguienteFase = enfrentamientoActual.getNumeroFase() / 2;
        int siguienteRonda = enfrentamientoActual.getRonda() + 1;

        // Buscar el enfrentamiento de la siguiente fase
        List<Enfrentamiento> enfrentamientosSiguienteFase = enfrentamientoRepository
            .findByPozoAndRonda(enfrentamientoActual.getPozo(), siguienteRonda);

        // Determinar en qué posición del bracket va el ganador
        int posicionEnBracket = calcularPosicionEnBracket(enfrentamientoActual, enfrentamientosSiguienteFase);

        if (posicionEnBracket >= 0 && posicionEnBracket < enfrentamientosSiguienteFase.size()) {
            Enfrentamiento siguienteEnfrentamiento = enfrentamientosSiguienteFase.get(posicionEnBracket);

            // Asignar el ganador a la posición correcta
            if (siguienteEnfrentamiento.getEquipo1() == null) {
                siguienteEnfrentamiento.setEquipo1(equipoGanador);
            } else if (siguienteEnfrentamiento.getEquipo2() == null) {
                siguienteEnfrentamiento.setEquipo2(equipoGanador);
            }

            enfrentamientoRepository.save(siguienteEnfrentamiento);

            logger.info("Equipo {} avanza a {}avos de final",
                       equipoGanador.getNombreEquipo(), siguienteFase);
        }
    }

    /**
     * Calcula la posición en el bracket donde debe ir el ganador.
     */
    private int calcularPosicionEnBracket(Enfrentamiento enfrentamientoActual,
                                          List<Enfrentamiento> enfrentamientosSiguienteFase) {
        // Simplificación: buscar el primer enfrentamiento con espacio
        for (int i = 0; i < enfrentamientosSiguienteFase.size(); i++) {
            Enfrentamiento siguiente = enfrentamientosSiguienteFase.get(i);
            if (siguiente.getEquipo1() == null || siguiente.getEquipo2() == null) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Obtiene el nombre de la fase según el número.
     */
    public String getNombreFase(int numeroFase) {
        switch (numeroFase) {
            case 64: return "Sesentaicuatroavos de final";
            case 32: return "Treintaidosavos de final";
            case 16: return "Dieciseisavos de final";
            case 8: return "Octavos de final";
            case 4: return "Cuartos de final";
            case 2: return "Semifinales";
            case 1: return "Final";
            default: return numeroFase + "avos de final";
        }
    }

    @Override
    public boolean validarNumeroEquipos(int numeroEquipos) {
        // Eliminación directa requiere mínimo 2 equipos
        return numeroEquipos >= 2;
    }

    @Override
    public int getMinEquipos() {
        return 2;
    }

    @Override
    public int getMaxEquiposRecomendado() {
        return 64; // Bracket de 64 equipos
    }

    @Override
    public int calcularNumeroPartidos(int numeroEquipos) {
        // En eliminación directa: n-1 partidos para determinar un ganador
        return numeroEquipos - 1;
    }

    @Override
    public String getDescripcion() {
        return "Modalidad Eliminación Directa: Los equipos compiten en un bracket de eliminación. " +
               "El perdedor queda eliminado y el ganador avanza a la siguiente fase. " +
               "Fases: 32avos, 16avos, Octavos, Cuartos, Semifinales y Final. " +
               "Si el número de equipos no es potencia de 2, se agregan 'byes' automáticamente.";
    }
}
