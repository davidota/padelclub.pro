package com.padellevel.services;

import com.padellevel.data.*;
import com.padellevel.repository.EstadisticaJugadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar estadísticas de jugadores individuales.
 *
 * Proporciona tres niveles de estadísticas:
 * - Globales: Todas las estadísticas del jugador en todos los torneos
 * - Por Torneo: Estadísticas del jugador en un torneo específico
 * - Por Pozo: Estadísticas del jugador en un pozo específico
 *
 * Las estadísticas se actualizan automáticamente después de cada partido.
 */
@Service
public class EstadisticaJugadorService {

    private static final Logger logger = LoggerFactory.getLogger(EstadisticaJugadorService.class);

    private final EstadisticaJugadorRepository estadisticaJugadorRepository;

    public EstadisticaJugadorService(EstadisticaJugadorRepository estadisticaJugadorRepository) {
        this.estadisticaJugadorRepository = estadisticaJugadorRepository;
    }

    /**
     * Actualiza las estadísticas de todos los jugadores involucrados en un partido.
     * Este método se llama automáticamente después de registrar un resultado.
     */
    @Transactional
    public void actualizarEstadisticasTrasPartido(Enfrentamiento enfrentamiento) {
        logger.info("Actualizando estadísticas tras partido {}", enfrentamiento.getId());

        if (enfrentamiento == null || enfrentamiento.getEstado() != EstadoEnfrentamiento.FINALIZADO) {
            logger.warn("El enfrentamiento no está finalizado, no se actualizan estadísticas");
            return;
        }

        Pozo pozo = enfrentamiento.getPozo();
        if (pozo == null) {
            logger.error("El enfrentamiento no tiene pozo asociado");
            return;
        }

        Torneo torneo = pozo.getTorneo();
        if (torneo == null) {
            logger.error("El pozo no tiene torneo asociado");
            return;
        }

        // Extraer jugadores de ambos equipos
        List<User> jugadores = extraerJugadores(enfrentamiento);

        if (jugadores.isEmpty()) {
            logger.warn("No se encontraron jugadores en el enfrentamiento");
            return;
        }

        // Determinar equipo ganador
        Integer equipoGanador = determinarEquipoGanador(enfrentamiento);

        // Calcular estadísticas del partido
        int[] setsGanados = calcularSetsGanados(enfrentamiento);
        int[] juegosGanados = calcularJuegosGanados(enfrentamiento);

        // Actualizar estadísticas para cada jugador
        for (User jugador : jugadores) {
            boolean esEquipo1 = perteneceAEquipo1(jugador, enfrentamiento);
            int indiceEquipo = esEquipo1 ? 0 : 1;

            boolean ganoPartido = (equipoGanador != null &&
                                  ((equipoGanador == 1 && esEquipo1) ||
                                   (equipoGanador == 2 && !esEquipo1)));

            boolean empatePartido = (equipoGanador == null);

            // Actualizar estadísticas globales
            actualizarEstadisticaGlobal(jugador, ganoPartido, empatePartido,
                                       setsGanados[indiceEquipo], setsGanados[1 - indiceEquipo],
                                       juegosGanados[indiceEquipo], juegosGanados[1 - indiceEquipo]);

            // Actualizar estadísticas del torneo
            actualizarEstadisticaTorneo(jugador, torneo, ganoPartido, empatePartido,
                                       setsGanados[indiceEquipo], setsGanados[1 - indiceEquipo],
                                       juegosGanados[indiceEquipo], juegosGanados[1 - indiceEquipo]);

            // Actualizar estadísticas del pozo
            actualizarEstadisticaPozo(jugador, pozo, ganoPartido, empatePartido,
                                     setsGanados[indiceEquipo], setsGanados[1 - indiceEquipo],
                                     juegosGanados[indiceEquipo], juegosGanados[1 - indiceEquipo]);
        }

        logger.info("Estadísticas actualizadas para {} jugadores", jugadores.size());
    }

    /**
     * Obtiene o crea la estadística global de un jugador.
     */
    @Transactional
    public EstadisticaJugador obtenerEstadisticaGlobal(User jugador) {
        return estadisticaJugadorRepository
            .findByJugadorAndTorneoIsNullAndPozoIsNull(jugador)
            .orElseGet(() -> {
                EstadisticaJugador nueva = new EstadisticaJugador();
                nueva.setJugador(jugador);
                inicializarEstadistica(nueva);
                return estadisticaJugadorRepository.save(nueva);
            });
    }

    /**
     * Obtiene o crea la estadística de un jugador en un torneo específico.
     */
    @Transactional
    public EstadisticaJugador obtenerEstadisticaTorneo(User jugador, Torneo torneo) {
        return estadisticaJugadorRepository
            .findByJugadorAndTorneoAndPozoIsNull(jugador, torneo)
            .orElseGet(() -> {
                EstadisticaJugador nueva = new EstadisticaJugador();
                nueva.setJugador(jugador);
                nueva.setTorneo(torneo);
                inicializarEstadistica(nueva);
                return estadisticaJugadorRepository.save(nueva);
            });
    }

    /**
     * Obtiene o crea la estadística de un jugador en un pozo específico.
     */
    @Transactional
    public EstadisticaJugador obtenerEstadisticaPozo(User jugador, Pozo pozo) {
        return estadisticaJugadorRepository
            .findByJugadorAndPozo(jugador, pozo)
            .orElseGet(() -> {
                EstadisticaJugador nueva = new EstadisticaJugador();
                nueva.setJugador(jugador);
                nueva.setPozo(pozo);
                nueva.setTorneo(pozo.getTorneo());
                inicializarEstadistica(nueva);
                return estadisticaJugadorRepository.save(nueva);
            });
    }

    /**
     * Obtiene todas las estadísticas de un jugador (global, torneos y pozos).
     */
    public List<EstadisticaJugador> obtenerTodasEstadisticas(User jugador) {
        return estadisticaJugadorRepository.findByJugador(jugador);
    }

    /**
     * Calcula el porcentaje de victorias de un jugador.
     */
    public Double calcularPorcentajeVictorias(EstadisticaJugador estadistica) {
        if (estadistica.getPartidosJugados() == 0) {
            return 0.0;
        }
        return (estadistica.getPartidosGanados() * 100.0) / estadistica.getPartidosJugados();
    }

    /**
     * Calcula el rendimiento del jugador (rating de 0 a 100).
     * Considera victorias, diferencia de juegos y diferencia de sets.
     */
    public Double calcularRendimiento(EstadisticaJugador estadistica) {
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

    /**
     * Calcula la racha actual del jugador (positiva o negativa).
     */
    public Integer calcularRacha(User jugador) {
        // Obtener últimos partidos del jugador ordenados por fecha
        List<Enfrentamiento> ultimosPartidos = obtenerUltimosPartidos(jugador, 10);

        if (ultimosPartidos.isEmpty()) {
            return 0;
        }

        int racha = 0;
        Boolean ultimoResultado = null;

        for (Enfrentamiento partido : ultimosPartidos) {
            Boolean gano = determinarSiGano(jugador, partido);

            if (gano == null) {
                continue; // Empate, no afecta racha
            }

            if (ultimoResultado == null) {
                ultimoResultado = gano;
                racha = gano ? 1 : -1;
            } else if (ultimoResultado == gano) {
                racha += gano ? 1 : -1;
            } else {
                break; // Se rompió la racha
            }
        }

        return racha;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Actualiza la estadística global del jugador.
     */
    private void actualizarEstadisticaGlobal(User jugador, boolean gano, boolean empate,
                                            int setsGanados, int setsPerdidos,
                                            int juegosGanados, int juegosPerdidos) {
        EstadisticaJugador stats = obtenerEstadisticaGlobal(jugador);
        actualizarEstadistica(stats, gano, empate, setsGanados, setsPerdidos,
                            juegosGanados, juegosPerdidos);
        estadisticaJugadorRepository.save(stats);
    }

    /**
     * Actualiza la estadística del jugador en un torneo.
     */
    private void actualizarEstadisticaTorneo(User jugador, Torneo torneo, boolean gano, boolean empate,
                                            int setsGanados, int setsPerdidos,
                                            int juegosGanados, int juegosPerdidos) {
        EstadisticaJugador stats = obtenerEstadisticaTorneo(jugador, torneo);
        actualizarEstadistica(stats, gano, empate, setsGanados, setsPerdidos,
                            juegosGanados, juegosPerdidos);
        estadisticaJugadorRepository.save(stats);
    }

    /**
     * Actualiza la estadística del jugador en un pozo.
     */
    private void actualizarEstadisticaPozo(User jugador, Pozo pozo, boolean gano, boolean empate,
                                          int setsGanados, int setsPerdidos,
                                          int juegosGanados, int juegosPerdidos) {
        EstadisticaJugador stats = obtenerEstadisticaPozo(jugador, pozo);
        actualizarEstadistica(stats, gano, empate, setsGanados, setsPerdidos,
                            juegosGanados, juegosPerdidos);
        estadisticaJugadorRepository.save(stats);
    }

    /**
     * Actualiza los contadores de una estadística.
     */
    private void actualizarEstadistica(EstadisticaJugador stats, boolean gano, boolean empate,
                                      int setsGanados, int setsPerdidos,
                                      int juegosGanados, int juegosPerdidos) {
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
        stats.setRacha(calcularRacha(stats.getJugador()));
    }

    /**
     * Inicializa una estadística nueva con valores por defecto.
     */
    private void inicializarEstadistica(EstadisticaJugador estadistica) {
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
        estadistica.setRacha(0);
        estadistica.setPosicion(0);
        estadistica.setPuntos(0);
    }

    /**
     * Extrae todos los jugadores de un enfrentamiento.
     */
    private List<User> extraerJugadores(Enfrentamiento enfrentamiento) {
        List<User> jugadores = new ArrayList<>();

        Equipo equipo1 = enfrentamiento.getEquipo1();
        Equipo equipo2 = enfrentamiento.getEquipo2();

        if (equipo1 != null) {
            if (equipo1.getParticipante1() != null) {
                jugadores.add(equipo1.getParticipante1());
            }
            if (equipo1.getParticipante2() != null) {
                jugadores.add(equipo1.getParticipante2());
            }
        }

        if (equipo2 != null) {
            if (equipo2.getParticipante1() != null) {
                jugadores.add(equipo2.getParticipante1());
            }
            if (equipo2.getParticipante2() != null) {
                jugadores.add(equipo2.getParticipante2());
            }
        }

        return jugadores;
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

    /**
     * Determina si un jugador pertenece al equipo 1.
     */
    private boolean perteneceAEquipo1(User jugador, Enfrentamiento enfrentamiento) {
        Equipo equipo1 = enfrentamiento.getEquipo1();

        if (equipo1 == null) {
            return false;
        }

        return (equipo1.getParticipante1() != null && equipo1.getParticipante1().getId().equals(jugador.getId())) ||
               (equipo1.getParticipante2() != null && equipo1.getParticipante2().getId().equals(jugador.getId()));
    }

    /**
     * Determina si un jugador ganó un partido específico.
     */
    private Boolean determinarSiGano(User jugador, Enfrentamiento partido) {
        Integer equipoGanador = determinarEquipoGanador(partido);

        if (equipoGanador == null) {
            return null; // Empate
        }

        boolean esEquipo1 = perteneceAEquipo1(jugador, partido);
        return (equipoGanador == 1 && esEquipo1) || (equipoGanador == 2 && !esEquipo1);
    }

    /**
     * Obtiene los últimos N partidos de un jugador.
     */
    private List<Enfrentamiento> obtenerUltimosPartidos(User jugador, int limite) {
        // TODO: Implementar consulta para obtener últimos partidos ordenados por fecha
        // Por ahora retorna lista vacía
        return new ArrayList<>();
    }
}
