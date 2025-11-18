package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EstadisticaEquipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar estadísticas de equipos.
 *
 * Proporciona tres niveles de estadísticas:
 * - Globales: Todas las estadísticas del equipo en todos los torneos
 * - Por Torneo: Estadísticas del equipo en un torneo específico
 * - Por Pozo: Estadísticas del equipo en un pozo específico
 *
 * NOTA: En modalidad Americano, las estadísticas de equipo no se actualizan
 * porque los equipos son temporales y cambian cada ronda.
 */
@Service
public class EstadisticaEquipoService {

    private static final Logger logger = LoggerFactory.getLogger(EstadisticaEquipoService.class);

    private final EstadisticaEquipoRepository estadisticaEquipoRepository;

    public EstadisticaEquipoService(EstadisticaEquipoRepository estadisticaEquipoRepository) {
        this.estadisticaEquipoRepository = estadisticaEquipoRepository;
    }

    /**
     * Actualiza las estadísticas de los equipos involucrados en un partido.
     * NO se actualiza para modalidad Americano (equipos temporales).
     */
    @Transactional
    public void actualizarEstadisticasTrasPartido(Enfrentamiento enfrentamiento) {
        logger.info("Actualizando estadísticas de equipo tras partido {}", enfrentamiento.getId());

        if (enfrentamiento == null || enfrentamiento.getEstado() != EstadoEnfrentamiento.FINALIZADO) {
            logger.warn("El enfrentamiento no está finalizado, no se actualizan estadísticas");
            return;
        }

        Pozo pozo = enfrentamiento.getPozo();
        if (pozo == null) {
            logger.error("El enfrentamiento no tiene pozo asociado");
            return;
        }

        // No actualizar estadísticas de equipo para Americano (equipos temporales)
        if (pozo.getModalidad() == ModalidadPozo.AMERICANO) {
            logger.debug("Modalidad Americano detectada, no se actualizan estadísticas de equipo");
            return;
        }

        Torneo torneo = pozo.getTorneo();
        if (torneo == null) {
            logger.error("El pozo no tiene torneo asociado");
            return;
        }

        Equipo equipo1 = enfrentamiento.getEquipo1();
        Equipo equipo2 = enfrentamiento.getEquipo2();

        if (equipo1 == null || equipo2 == null) {
            logger.error("El enfrentamiento no tiene ambos equipos definidos");
            return;
        }

        // Determinar equipo ganador
        Integer equipoGanador = determinarEquipoGanador(enfrentamiento);

        // Calcular estadísticas del partido
        int[] setsGanados = calcularSetsGanados(enfrentamiento);
        int[] juegosGanados = calcularJuegosGanados(enfrentamiento);

        boolean ganoEquipo1 = (equipoGanador != null && equipoGanador == 1);
        boolean ganoEquipo2 = (equipoGanador != null && equipoGanador == 2);
        boolean empate = (equipoGanador == null);

        // Actualizar estadísticas del equipo 1
        actualizarEstadisticaGlobal(equipo1, ganoEquipo1, empate,
                                   setsGanados[0], setsGanados[1],
                                   juegosGanados[0], juegosGanados[1]);

        actualizarEstadisticaTorneo(equipo1, torneo, ganoEquipo1, empate,
                                   setsGanados[0], setsGanados[1],
                                   juegosGanados[0], juegosGanados[1], pozo.getModalidad());

        actualizarEstadisticaPozo(equipo1, pozo, ganoEquipo1, empate,
                                 setsGanados[0], setsGanados[1],
                                 juegosGanados[0], juegosGanados[1]);

        // Actualizar estadísticas del equipo 2
        actualizarEstadisticaGlobal(equipo2, ganoEquipo2, empate,
                                   setsGanados[1], setsGanados[0],
                                   juegosGanados[1], juegosGanados[0]);

        actualizarEstadisticaTorneo(equipo2, torneo, ganoEquipo2, empate,
                                   setsGanados[1], setsGanados[0],
                                   juegosGanados[1], juegosGanados[0], pozo.getModalidad());

        actualizarEstadisticaPozo(equipo2, pozo, ganoEquipo2, empate,
                                 setsGanados[1], setsGanados[0],
                                 juegosGanados[1], juegosGanados[0]);

        logger.info("Estadísticas de equipo actualizadas para {} y {}",
                   equipo1.getNombreEquipo(), equipo2.getNombreEquipo());
    }

    /**
     * Obtiene o crea la estadística global de un equipo.
     */
    @Transactional
    public EstadisticaEquipo obtenerEstadisticaGlobal(Equipo equipo) {
        return estadisticaEquipoRepository
            .findByEquipoAndTorneoIsNullAndPozoIsNull(equipo)
            .orElseGet(() -> {
                EstadisticaEquipo nueva = new EstadisticaEquipo();
                nueva.setEquipo(equipo);
                inicializarEstadistica(nueva);
                return estadisticaEquipoRepository.save(nueva);
            });
    }

    /**
     * Obtiene o crea la estadística de un equipo en un torneo específico.
     */
    @Transactional
    public EstadisticaEquipo obtenerEstadisticaTorneo(Equipo equipo, Torneo torneo) {
        return estadisticaEquipoRepository
            .findByEquipoAndTorneoAndPozoIsNull(equipo, torneo)
            .orElseGet(() -> {
                EstadisticaEquipo nueva = new EstadisticaEquipo();
                nueva.setEquipo(equipo);
                nueva.setTorneo(torneo);
                inicializarEstadistica(nueva);
                return estadisticaEquipoRepository.save(nueva);
            });
    }

    /**
     * Obtiene o crea la estadística de un equipo en un pozo específico.
     */
    @Transactional
    public EstadisticaEquipo obtenerEstadisticaPozo(Equipo equipo, Pozo pozo) {
        return estadisticaEquipoRepository
            .findByEquipoAndPozo(equipo, pozo)
            .orElseGet(() -> {
                EstadisticaEquipo nueva = new EstadisticaEquipo();
                nueva.setEquipo(equipo);
                nueva.setPozo(pozo);
                nueva.setTorneo(pozo.getTorneo());
                inicializarEstadistica(nueva);
                return estadisticaEquipoRepository.save(nueva);
            });
    }

    /**
     * Obtiene todas las estadísticas de un equipo.
     */
    public List<EstadisticaEquipo> obtenerTodasEstadisticas(Equipo equipo) {
        return estadisticaEquipoRepository.findByEquipo(equipo);
    }

    /**
     * Calcula el porcentaje de victorias de un equipo.
     */
    public Double calcularPorcentajeVictorias(EstadisticaEquipo estadistica) {
        if (estadistica.getPartidosJugados() == 0) {
            return 0.0;
        }
        return (estadistica.getPartidosGanados() * 100.0) / estadistica.getPartidosJugados();
    }

    /**
     * Calcula los puntos del equipo según la modalidad.
     * - Round Robin: 3 puntos por victoria, 1 por empate, 0 por derrota
     * - Otras modalidades: no se usan puntos
     */
    public int calcularPuntos(EstadisticaEquipo estadistica, ModalidadPozo modalidad) {
        if (modalidad == ModalidadPozo.TODOS_CONTRA_TODOS) {
            return (estadistica.getPartidosGanados() * 3) +
                   (estadistica.getPartidosEmpatados() * 1);
        }
        return 0;
    }

    /**
     * Calcula el rendimiento del equipo (rating de 0 a 100).
     */
    public Double calcularRendimiento(EstadisticaEquipo estadistica) {
        if (estadistica.getPartidosJugados() == 0) {
            return 0.0;
        }

        // Porcentaje de victorias (peso 40%)
        double porcentajeVictorias = calcularPorcentajeVictorias(estadistica);

        // Diferencial de juegos normalizado (peso 30%)
        int diferencialJuegos = estadistica.getJuegosGanados() - estadistica.getJuegosPerdidos();
        double factorJuegos = Math.min(100.0, Math.max(0.0, 50.0 + (diferencialJuegos * 2.0)));

        // Diferencial de sets normalizado (peso 30%)
        int diferencialSets = estadistica.getSetsGanados() - estadistica.getSetsPerdidos();
        double factorSets = Math.min(100.0, Math.max(0.0, 50.0 + (diferencialSets * 5.0)));

        return (porcentajeVictorias * 0.4) + (factorJuegos * 0.3) + (factorSets * 0.3);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Actualiza la estadística global del equipo.
     */
    private void actualizarEstadisticaGlobal(Equipo equipo, boolean gano, boolean empate,
                                            int setsGanados, int setsPerdidos,
                                            int juegosGanados, int juegosPerdidos) {
        EstadisticaEquipo stats = obtenerEstadisticaGlobal(equipo);
        actualizarEstadistica(stats, gano, empate, setsGanados, setsPerdidos,
                            juegosGanados, juegosPerdidos, null);
        estadisticaEquipoRepository.save(stats);
    }

    /**
     * Actualiza la estadística del equipo en un torneo.
     */
    private void actualizarEstadisticaTorneo(Equipo equipo, Torneo torneo, boolean gano, boolean empate,
                                            int setsGanados, int setsPerdidos,
                                            int juegosGanados, int juegosPerdidos,
                                            ModalidadPozo modalidad) {
        EstadisticaEquipo stats = obtenerEstadisticaTorneo(equipo, torneo);
        actualizarEstadistica(stats, gano, empate, setsGanados, setsPerdidos,
                            juegosGanados, juegosPerdidos, modalidad);
        estadisticaEquipoRepository.save(stats);
    }

    /**
     * Actualiza la estadística del equipo en un pozo.
     */
    private void actualizarEstadisticaPozo(Equipo equipo, Pozo pozo, boolean gano, boolean empate,
                                          int setsGanados, int setsPerdidos,
                                          int juegosGanados, int juegosPerdidos) {
        EstadisticaEquipo stats = obtenerEstadisticaPozo(equipo, pozo);
        actualizarEstadistica(stats, gano, empate, setsGanados, setsPerdidos,
                            juegosGanados, juegosPerdidos, pozo.getModalidad());
        estadisticaEquipoRepository.save(stats);
    }

    /**
     * Actualiza los contadores de una estadística.
     */
    private void actualizarEstadistica(EstadisticaEquipo stats, boolean gano, boolean empate,
                                      int setsGanados, int setsPerdidos,
                                      int juegosGanados, int juegosPerdidos,
                                      ModalidadPozo modalidad) {
        stats.setPartidosJugados(stats.getPartidosJugados() + 1);

        if (gano) {
            stats.setPartidosGanados(stats.getPartidosGanados() + 1);
        } else if (empate) {
            stats.setPartidosEmpatados(stats.getPartidosEmpatados() + 1);
        } else {
            stats.setPartidosPerdidos(stats.getPartidosPerdidos() + 1);
        }

        stats.setSetsGanados(stats.getSetsGanados() + setsGanados);
        stats.setSetsPerdidos(stats.getSetsPerdidos() + setsPerdidos);
        stats.setJuegosGanados(stats.getJuegosGanados() + juegosGanados);
        stats.setJuegosPerdidos(stats.getJuegosPerdidos() + juegosPerdidos);

        // Actualizar campos calculados
        stats.setPorcentajeVictorias(calcularPorcentajeVictorias(stats));
        stats.setRendimiento(calcularRendimiento(stats));

        // Actualizar puntos si es modalidad Round Robin
        if (modalidad != null) {
            stats.setPuntos(calcularPuntos(stats, modalidad));
        }
    }

    /**
     * Inicializa una estadística nueva con valores por defecto.
     */
    private void inicializarEstadistica(EstadisticaEquipo estadistica) {
        estadistica.setPartidosJugados(0);
        estadistica.setPartidosGanados(0);
        estadistica.setPartidosPerdidos(0);
        estadistica.setPartidosEmpatados(0);
        estadistica.setSetsGanados(0);
        estadistica.setSetsPerdidos(0);
        estadistica.setJuegosGanados(0);
        estadistica.setJuegosPerdidos(0);
        estadistica.setPorcentajeVictorias(0.0);
        estadistica.setRendimiento(0.0);
        estadistica.setPosicion(0);
        estadistica.setPuntos(0);
    }

    /**
     * Determina el equipo ganador de un enfrentamiento.
     * @return 1 si ganó equipo1, 2 si ganó equipo2, null si empate
     */
    private Integer determinarEquipoGanador(Enfrentamiento enfrentamiento) {
        Integer setsEquipo1 = enfrentamiento.getSetsEquipo1();
        Integer setsEquipo2 = enfrentamiento.getSetsEquipo2();

        if (setsEquipo1 == null || setsEquipo2 == null) {
            return null;
        }

        if (setsEquipo1 > setsEquipo2) {
            return 1;
        } else if (setsEquipo2 > setsEquipo1) {
            return 2;
        } else {
            return null; // Empate
        }
    }

    /**
     * Calcula los sets ganados por cada equipo.
     */
    private int[] calcularSetsGanados(Enfrentamiento enfrentamiento) {
        int[] sets = new int[2];

        if (enfrentamiento.getSetsEquipo1() != null) {
            sets[0] = enfrentamiento.getSetsEquipo1();
        }
        if (enfrentamiento.getSetsEquipo2() != null) {
            sets[1] = enfrentamiento.getSetsEquipo2();
        }

        return sets;
    }

    /**
     * Calcula los juegos ganados por cada equipo sumando todos los sets.
     */
    private int[] calcularJuegosGanados(Enfrentamiento enfrentamiento) {
        int[] juegos = new int[2];

        List<SetEntity> sets = enfrentamiento.getSets();
        if (sets != null) {
            for (SetEntity set : sets) {
                juegos[0] += (set.getJuegosEquipo1() != null ? set.getJuegosEquipo1() : 0);
                juegos[1] += (set.getJuegosEquipo2() != null ? set.getJuegosEquipo2() : 0);
            }
        }

        return juegos;
    }
}
